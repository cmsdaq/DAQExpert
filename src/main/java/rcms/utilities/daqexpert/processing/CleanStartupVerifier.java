package rcms.utilities.daqexpert.processing;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.segmentation.DataResolution;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class CleanStartupVerifier {

    private static Logger logger = Logger.getLogger(CleanStartupVerifier.class);

    private PersistenceManager persistenceManager;

    protected int observationInterval = 1000;
    protected int observationSteps = 30;

    public CleanStartupVerifier(PersistenceManager persistenceManager){
        this.persistenceManager = persistenceManager;
    }

    public void ensureSafeStartupProcedure(){
        Condition lastVersionEntry = this.persistenceManager.getLastVersionEntry();

        if(lastVersionEntry != null) {
            logger.info("Checking last version entry " +lastVersionEntry.getTitle() + ": " + lastVersionEntry.getStart() + " - " + lastVersionEntry.getEnd() + ")");
        }
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        Date now = calendar.getTime();
        calendar.add(Calendar.MINUTE, -5);
        Date start = calendar.getTime();
        calendar.add(Calendar.SECOND, 6);
        Date end = calendar.getTime();
        logger.info("Checking existence of recently produced conditions (" + start.toString() + " - " + end.toString() + ")");
        Set<Condition> recentUnfinished = this.persistenceManager.getEntriesPlain(start, end).stream()
                .filter(c ->c.getEnd() == null).filter(c->c.getGroup() != ConditionGroup.EXPERT_VERSION).collect(Collectors.toSet());
        logger.info("Number of unfinished condition recently: " + recentUnfinished.size());

        if(lastVersionEntry == null){
            logger.info("No information about last DAQExpert run");
            if(recentUnfinished.size() == 0){
                logger.info("No unfinished entries - safe to continue startup procedure");
            } else{
                throw new RuntimeException("Some entries in database are unfinished");
                //TODO: clenup
            }
        } else {
            if (lastVersionEntry.getEnd() == null) {
				/* There is some version running in the moment, prevent startup */
				/* It's possible that previous process crashed and no db is running - but the expert didn't close properly.
				This could be found out by checking if raw entries are produced */
                logger.warn("There is DAQExpert running or previous one didn't finish gracefully. Observing the system for "+(observationInterval* observationSteps)+" ms");

                boolean observedOtherProcessRunning = false;
                long rawEntriesCount = this.persistenceManager.getRawData(start, end, DataResolution.Full).stream().count();
                for (int i = 0; i < observationSteps; i++) {
                    long currentRawEntriesCount = this.persistenceManager.getRawData(start, end, DataResolution.Full).stream().count();
                    if (currentRawEntriesCount != rawEntriesCount) {
					/* Number of raw entries in database changed  - other process must be running*/
                        observedOtherProcessRunning = true;
                        break;
                    }
                    try {
                        Thread.sleep(observationInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (observedOtherProcessRunning) {
                    logger.warn("Cannot run more than one DAQExpert instances with the same database. Startup procedure interrupted.");
                    throw new RuntimeException("Startup procedure interrupted. Some DAQExpert process is still running.");
                } else {
                    logger.warn("Previous DAQExpert process didn't finish gracefully. Cleaning up the database. The startup procedure continues.");
                    recentUnfinished.stream().forEach(c -> c.setEnd(now));
                    recentUnfinished.stream().forEach(c -> c.calculateDuration());
                    recentUnfinished.stream().forEach(c->this.persistenceManager.update(c));

                    lastVersionEntry.setEnd(now);
                    this.persistenceManager.update(lastVersionEntry);
                    logger.info("Cleanup successfully finished. Updated " + recentUnfinished.size() + " condition entries and version "+ lastVersionEntry.getTitle()+" entry");
                }


            } else {
			/* No DAQ expert is not running but it's possible that some entries are unfinished - DO cleanup the database */
                logger.info("Last DAQExpert process ended " + lastVersionEntry.getEnd());
                if (recentUnfinished.size() > 0) {
				/* Exists some unfinished conditions. */
                    logger.warn("Exists some unfinished entries in the database: " + recentUnfinished.stream().map(c -> c.getTitle()).collect(Collectors.toList()));
                    recentUnfinished.stream().forEach(c -> c.setEnd(now));
                    recentUnfinished.stream().forEach(c -> c.calculateDuration());
                    recentUnfinished.stream().forEach(c->this.persistenceManager.update(c));
                    logger.info("Cleanup successfully finished. Updated "+ recentUnfinished.size()+" entries.");


                } else {
                    logger.info("Clear startup.");
                }

            }
        }
    }
}
