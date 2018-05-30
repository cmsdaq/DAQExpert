package rcms.utilities.daqexpert.persistence;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

import java.util.Date;

/**
 * Persists dominating condition in order to see what was presented as a root case.
 */
public class DominatingPersistor {


    private final static Logger logger = Logger.getLogger(DominatingPersistor.class);

    private final PersistenceManager persistenceManager;

    private Condition previousDominatingEntry;

    public DominatingPersistor(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }


    /**
     * Persist dominating selection. Following cases are possible:
     * <p>
     * 1. no previous (P) dominating condition. Current (C) is persisted as result (R)
     * <pre>
     * P.
     * C. ####
     * R. CCCC
     * </pre>
     * <p>
     * 2. Current preempts previous. End date of previous = start of current
     * <pre>
     * P. ####
     * C.   #####
     * R. PPCCCCC
     * </pre>
     * <p>
     * 3. Current preempts previous and ends before previous.
     * <pre>
     * P. #########
     * C.     ###
     * R. PPPPCCCPP
     * </pre>
     * <p>
     * 4. No current dominating
     * <pre>
     *     P. ##
     *     C
     * </pre>
     *
     * @param previousDominating
     * @param currentlyDominating
     * @param transitionTime
     */
    public void persistDominating(Condition previousDominating, Condition currentlyDominating, Date transitionTime) {

        Date previousEnds;
        Date currentStarts;

        logger.info("Persisting dominating for " + (previousDominating != null ? previousDominating.getTitle() : "null") + " and " + (currentlyDominating != null ? currentlyDominating.getTitle() : "null"));

        // case 1
        if (previousDominating == null) {

            currentStarts = transitionTime;
            previousEnds = null; // there is no previous

        } else {


            // case 4
            if (currentlyDominating == null) {

                currentStarts = null;
                previousEnds = transitionTime;
            }


            // case 2
            else if (previousDominating.getStart().getTime() < currentlyDominating.getStart().getTime()) {

                currentStarts = transitionTime;
                previousEnds = currentStarts;
            }

            // case 3
            else {

                currentStarts = transitionTime;
                previousEnds = currentStarts;

            }

        }

        if (previousDominatingEntry != null && previousEnds != null) {
            previousDominatingEntry.setEnd(previousEnds);
            previousDominatingEntry.calculateDuration();
            this.persistenceManager.update(previousDominatingEntry);
            logger.info("  - updating previous entry with end date: " + previousEnds);

        }

        if (currentStarts != null) {
            Condition dominatingEntry = new Condition();
            dominatingEntry.setTitle(currentlyDominating.getTitle());

            LogicModule producer = currentlyDominating.getProducer();
            if (producer != null && producer.getBriefDescription() != null) {
                dominatingEntry.setDescription(currentlyDominating.getProducer().getBriefDescription());
            } else {
                dominatingEntry.setDescription(currentlyDominating.getDescription());
            }

            if (currentlyDominating.getContext() != null) {
                dominatingEntry.setContext(currentlyDominating.getContext());
            }

            dominatingEntry.setMature(true);
            dominatingEntry.setClassName(ConditionPriority.DEFAULTT);
            dominatingEntry.setGroup(ConditionGroup.DOMINATING);
            dominatingEntry.setStart(currentStarts);

            this.persistenceManager.persist(dominatingEntry);

            previousDominatingEntry = dominatingEntry;

            logger.info("  - updating current entry with everything");
        } else {
            previousDominatingEntry = null;
            logger.info("  - throwing away previous entry");
        }


    }
}
