package rcms.utilities.daqexpert.persistence;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.processing.context.*;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

            updateDescriptionAndContext(previousDominating, previousDominatingEntry);
            this.persistenceManager.update(previousDominatingEntry);
            logger.info("  - updating previous entry with end date: " + previousEnds);

        }

        if (currentStarts != null) {
            Condition dominatingEntry = new Condition();
            dominatingEntry.setTitle(currentlyDominating.getTitle());

            updateDescriptionAndContext(currentlyDominating, dominatingEntry);

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

    private void updateDescriptionAndContext(Condition baseCondition, Condition dominatingEntry) {
        LogicModule producer = baseCondition.getProducer();
        if (producer != null && producer.getBriefDescription() != null) {
            dominatingEntry.setDescription(baseCondition.getProducer().getBriefDescription());
        } else {
            dominatingEntry.setDescription(baseCondition.getDescription());
        }


        Map<String, ContextEntry> baseContext = null;
        if(baseCondition.getProducer() instanceof ContextLogicModule && ((ContextLogicModule)baseCondition.getProducer()).getContextHandler().getContext() != null)
            baseContext = ((ContextLogicModule)baseCondition.getProducer()).getContextHandler().getContext().getContextEntryMap();

        if (baseContext!= null) {

            if (dominatingEntry.getContext() == null) {
                dominatingEntry.setContext(new HashMap<>());
            }


            for (Map.Entry<String, ContextEntry> entry : baseContext.entrySet()) {

                ContextEntry value = entry.getValue();
                String key = entry.getKey();

                if (dominatingEntry.getContext().containsKey(key)) {

                    if (value instanceof StatisticContextEntry) {
                        StatisticContextEntry v = (StatisticContextEntry) value;
                        ((StatisticContextEntry) dominatingEntry.getContext().get(key)).setMin(v.getMin());
                        ((StatisticContextEntry) dominatingEntry.getContext().get(key)).setMax(v.getMax());
                        ((StatisticContextEntry) dominatingEntry.getContext().get(key)).setAvg(v.getAvg());
                        ((StatisticContextEntry) dominatingEntry.getContext().get(key)).setCurrent(v.getCurrent());
                        ((StatisticContextEntry) dominatingEntry.getContext().get(key)).setUnit(v.getUnit());


                    } else if (value instanceof ObjectContextEntry) {
                        ObjectContextEntry v = (ObjectContextEntry) value;
                        ((ObjectContextEntry) dominatingEntry.getContext().get(key)).setTextRepresentationSet(v.getTextRepresentationSet());


                    } else if (value instanceof OptionalContextEntry) {
                        ObjectContextEntry v = (ObjectContextEntry) value;

                    }
                } else {

                    dominatingEntry.getContext().put(key, (ContextEntry) org.apache.commons.lang.SerializationUtils.clone(value));
                    dominatingEntry.getContext().get(key).setId(null);
                }

            }

        }


    }
}




