package rcms.utilities.daqexpert.persistence;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

import java.util.Date;

public class DominatingPersistor {


    private final static Logger logger = Logger.getLogger(DominatingPersistor.class);

    private final PersistenceManager persistenceManager;

    private Condition previousDominatingEntry;

    public DominatingPersistor(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }


    /**
     * Persist dominating selection. Following cases are possible:
     *
     * 1. no previous (P) dominating condition. Current (C) is persisted as result (R)
     * <pre>
     * P.
     * C. ####
     * R. CCCC
     * </pre>
     *
     * 2. Current preempts previous. End date of previous = start of current
     * <pre>
     * P. ####
     * C.   #####
     * R. PPCCCCC
     * </pre>
     *
     * 3. Current preempts previous and ends before previous.
     * <pre>
     * P. #########
     * C.     ###
     * R. PPPPCCCPP
     * </pre>
     *
     * @param previousDominating
     * @param currentlyDominating
     */
    public void persistDominating(Condition previousDominating, Condition currentlyDominating) {

        Date previousEnds;
        Date currentStarts;

        logger.info("Persisting dominating for " + (previousDominating != null ? previousDominating.getTitle(): "null" )+ " and " + (currentlyDominating != null ? currentlyDominating.getTitle(): "null" ));

        // case 1
        if(previousDominating == null){

            currentStarts = currentlyDominating.getStart();
            previousEnds = null; // there is no previous

        } else{


            // case 2
            if(previousDominating.getStart().getTime() < currentlyDominating.getStart().getTime()){

                currentStarts = currentlyDominating.getStart();
                previousEnds = currentStarts;
            }

            // case 3
            else{

                currentStarts = previousDominating.getEnd();
                previousEnds = currentStarts;

            }

        }

        Condition dominatingEntry = new Condition();
        dominatingEntry.setTitle(currentlyDominating.getTitle());
        String previousDominatingTitle = "empty";
        if(previousDominating != null){
            previousDominatingTitle = previousDominating.getTitle();
        }
        dominatingEntry.setDescription(currentlyDominating.getTitle() + " dominated previous " + previousDominatingTitle);

        dominatingEntry.setMature(true);
        dominatingEntry.setClassName(ConditionPriority.DEFAULTT);
        dominatingEntry.setGroup(ConditionGroup.DOMINATING);
        dominatingEntry.setStart(currentStarts);

        this.persistenceManager.persist(dominatingEntry);

        if(previousDominatingEntry!= null && previousEnds != null){
            previousDominatingEntry.setEnd(previousEnds);
            previousDominatingEntry.calculateDuration();
            this.persistenceManager.update(previousDominatingEntry);
        }

        previousDominatingEntry = dominatingEntry;
    }
}
