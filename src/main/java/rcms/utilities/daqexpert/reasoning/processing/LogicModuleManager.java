package rcms.utilities.daqexpert.reasoning.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.UnidentifiedFailure;

/**
 * Manager of checking process
 *
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class LogicModuleManager {

    private final static Logger logger = Logger.getLogger(LogicModuleManager.class);
    private final List<SimpleLogicModule> checkers = new ArrayList<>();

    private final List<ComparatorLogicModule> comparators = new ArrayList<>();

    private final ConditionProducer conditionProducer;

    private ExperimentalProcessor experimentalProcessor;

    /**
     * Constructor, order of checker matters. Checkers may use results of
     * checkers added before.
     *
     * @param conditionProducer daq object to analyze
     */
    public LogicModuleManager(ConditionProducer conditionProducer) {

        this.conditionProducer = conditionProducer;

        HashSet<String> knownFailureClasses = new HashSet<String>();

        for (LogicModuleRegistry lm : LogicModuleRegistry.getModulesInRunOrder()) {
            if (lm.getLogicModule() != null) {
                lm.getLogicModule().setLogicModuleRegistry(lm);
                if (lm.getLogicModule() instanceof SimpleLogicModule) {
                    SimpleLogicModule simpleLogicModule = (SimpleLogicModule) lm.getLogicModule();
                    checkers.add(simpleLogicModule);
                } else if (lm.getLogicModule() instanceof ComparatorLogicModule) {
                    ComparatorLogicModule comparatorLogicModule = (ComparatorLogicModule) lm.getLogicModule();
                    comparators.add(comparatorLogicModule);
                }

                if (lm.getLogicModule() instanceof Parameterizable) {
                    Parameterizable updatable = (Parameterizable) lm.getLogicModule();

                    updatable.parametrize(Application.get().getProp());
                    logger.info("LM " + updatable.getClass().getSimpleName() + " successfully parametrized");
                }

                if (lm.getLogicModule() instanceof KnownFailure) {
                    knownFailureClasses.add(lm.getLogicModule().getClass().getSimpleName());
                }

            } else {
                logger.info("This is not used: " + lm);
            }
        }

        logger.info("Registering " + knownFailureClasses.size()
                + " known-failure logic modules to covering unidentified-failure logic module");
        UnidentifiedFailure unidentifiedFailure = (UnidentifiedFailure) LogicModuleRegistry.UnidentifiedFailure
                .getLogicModule();
        unidentifiedFailure.setKnownFailureClasses(knownFailureClasses);

        try {
            experimentalProcessor = new ExperimentalProcessor(Application.get().getProp(Setting.EXPERIMENTAL_DIR));
            // experimentalProcessor.loadExperimentalLogicModules();
        } catch (IOException | ResourceException | ScriptException e) {
            experimentalProcessor = null;
            e.printStackTrace();
        }

    }

    /**
     * Run all logic modules for current snapshot
     *
     * @param daq current snapshot
     * @return results of logic modules analysis
     */
    public List<Condition> runLogicModules(DAQ daq, boolean includeExperimental) {

        List<Condition> results = new ArrayList<>();

        logger.debug("Running analysis modules for run " + daq.getSessionId());

        results.addAll(runCheckers(daq, includeExperimental));
        results.addAll(runComparators(daq));

        return results;
    }

    /**
     * Run checkers for current snapshot
     *
     * @param daq                 current snapshot
     * @param includeExperimental
     * @return results of checkers analysis including
     */
    private List<Condition> runCheckers(DAQ daq, boolean includeExperimental) {
        List<Condition> results = new ArrayList<>();
        HashMap<String, Boolean> checkerResultMap = new HashMap<>();

        for (SimpleLogicModule checker : checkers) {
            try {
                boolean result = checker.satisfied(daq, checkerResultMap);
                postprocess(checkerResultMap, checker, result, daq, results);
            } catch(RuntimeException e){
                logger.error("Logic module " + checker.getClass().getSimpleName() + " failed with: " + e.getClass());
            }
        }

        if (includeExperimental) {
            try {
                List<Pair<LogicModule, Boolean>> a = experimentalProcessor.runLogicModules(daq, checkerResultMap);

                logger.debug("Experimental logic modules returned: " + a);
                for (Pair<LogicModule, Boolean> b : a) {
                    postprocess(checkerResultMap, b.getLeft(), b.getRight(), daq, results);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        }
        results.addAll(conditionProducer.getFinishedThisRound());
        conditionProducer.clearFinishedThisRound();

        return results;
    }

    /**
     * @param checkerResultMap
     * @param checker
     * @param result
     * @param daq
     * @param results
     */
    private void postprocess(Map<String, Boolean> checkerResultMap, LogicModule checker, boolean result, DAQ daq,
                             List<Condition> results) {
        Date curr = null;
        checkerResultMap.put(checker.getClass().getSimpleName(), result);
        curr = new Date(daq.getLastUpdate());

        if (checker instanceof SimpleLogicModule) {
            SimpleLogicModule simpleChecker = (SimpleLogicModule) checker;

            if (result) {
                simpleChecker.increaseMaturity();
            } else {
                simpleChecker.resetMaturity();
            }

            Pair<Boolean, Condition> produceResult = conditionProducer.produce(simpleChecker, result, curr);


			/*
             * The event finishes (result = false), Context to be cleared for
			 * next events. Note that this is performed after
			 * EventProducer.produce so that context can be used to close the
			 * event
			 */
            if (checker instanceof ContextLogicModule) {
                ContextLogicModule contextLogicModule = ((ContextLogicModule) checker);
                contextLogicModule.getContext().triggerReady();
                if (!result) {
                    contextLogicModule.getContext().clearContext();
                }
                produceResult.getRight().publishUpdate();
            }

            if (produceResult.getLeft()) {
                results.add(produceResult.getRight());
            }
        }

    }

    /**
     * Run comparators for current snapshot
     *
     * @param daq current snapshot
     * @return results of checkers analysis
     */
    private List<Condition> runComparators(DAQ daq) {
        List<Condition> results = new ArrayList<>();
        for (ComparatorLogicModule comparator : comparators) {
            logger.trace("Running comparator " + comparator.getClass().getSimpleName());

            boolean result = comparator.compare(daq);
            Date current = new Date(comparator.getLast().getLastUpdate());

            Pair<Boolean, Condition> produced = conditionProducer.produce(comparator, result, current);
            if (produced.getLeft()) {
                logger.trace(produced.getRight());
                results.add(produced.getRight());
            }

        }
        return results;
    }

    public ExperimentalProcessor getExperimentalProcessor() {
        return experimentalProcessor;
    }

}
