package rcms.utilities.daqexpert.reasoning.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.logic.basic.AvoidableDowntime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Deadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Downtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.RateOutOfRange;
import rcms.utilities.daqexpert.reasoning.logic.basic.RunOngoing;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.basic.Transition;
import rcms.utilities.daqexpert.reasoning.logic.basic.WarningInSubsystem;
import rcms.utilities.daqexpert.reasoning.logic.comparators.DAQStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LHCBeamModeComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LHCMachineModeComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LevelZeroStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.RunComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.SessionComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.TCDSStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase1;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase2;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase3;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase4;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase5;

/**
 * Manager of checking process
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class LogicModuleManager {

	private final static Logger logger = Logger.getLogger(LogicModuleManager.class);
	private final List<SimpleLogicModule> checkers = new ArrayList<>();

	private final List<ComparatorLogicModule> comparators = new ArrayList<>();

	private final EventProducer eventProducer;

	private ExperimentalProcessor experimentalProcessor;
	private boolean artificialForced;

	/**
	 * Constructor, order of checker matters. Checkers may use results of
	 * checkers added before.
	 * 
	 * @param daq
	 *            daq object to analyze
	 */
	public LogicModuleManager(EventProducer eventProducer) {
		this.eventProducer = eventProducer;
		// Level 0 Independent
		checkers.add(new RateOutOfRange());
		checkers.add(new NoRate());
		checkers.add(new RunOngoing());
		checkers.add(new ExpectedRate());
		checkers.add(new Transition());
		checkers.add(new WarningInSubsystem());
		checkers.add(new StableBeams());

		// Level 1 (depends on L0)
		checkers.add(new NoRateWhenExpected());
		checkers.add(new Downtime());
		checkers.add(new Deadtime());
		checkers.add(new AvoidableDowntime());

		// Level 2 (depends on L1)
		checkers.add(new FlowchartCase1());
		checkers.add(new FlowchartCase2());
		checkers.add(new FlowchartCase3());
		checkers.add(new FlowchartCase4());
		checkers.add(new FlowchartCase5());
		// checkers.add(new FlowchartCase6());

		/* START EXPERIMENTAL LMs */
		// checkers.add(new YourNewLM());
		/* END EXPERIMENTAL LMs */

		// comparators
		comparators.add(new SessionComparator());
		comparators.add(new LHCBeamModeComparator());
		comparators.add(new LHCMachineModeComparator());
		comparators.add(new RunComparator());
		comparators.add(new LevelZeroStateComparator());
		comparators.add(new TCDSStateComparator());
		comparators.add(new DAQStateComparator());
		// comparators.add(new EVMComparator());

		try {
			experimentalProcessor = new ExperimentalProcessor(
					Application.get().getProp().getProperty(Application.EXPERIMENTAL_DIR));
			// experimentalProcessor.loadExperimentalLogicModules();
		} catch (IOException | ResourceException | ScriptException e) {
			experimentalProcessor = null;
			e.printStackTrace();
		}

		artificialForced = true;
	}

	/**
	 * Run all logic modules for current snapshot
	 * 
	 * @param daq
	 *            current snapshot
	 * @return results of logic modules analysis
	 */
	public List<Entry> runLogicModules(DAQ daq, boolean includeExperimental) {

		List<Entry> results = new ArrayList<>();

		logger.debug("Running analysis modules for run " + daq.getSessionId());

		results.addAll(runCheckers(daq, includeExperimental));
		results.addAll(runComparators(daq));

		return results;
	}

	/**
	 * Run checkers for current snapshot
	 * 
	 * @param daq
	 *            current snapshot
	 * @param includeExperimental
	 * @return results of checkers analysis
	 */
	private List<Entry> runCheckers(DAQ daq, boolean includeExperimental) {
		List<Entry> results = new ArrayList<>();
		HashMap<String, Boolean> checkerResultMap = new HashMap<>();

		for (SimpleLogicModule checker : checkers) {
			boolean result = checker.satisfied(daq, checkerResultMap);
			postprocess(checkerResultMap, checker, result, daq, results);
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
		results.addAll(eventProducer.getFinishedThisRound());
		eventProducer.clearFinishedThisRound();

		return results;
	}

	private void postprocess(Map<String, Boolean> checkerResultMap, LogicModule checker, boolean result, DAQ daq,
			List<Entry> results) {
		Date curr = null;
		checkerResultMap.put(checker.getClass().getSimpleName(), result);
		curr = new Date(daq.getLastUpdate());

		if (checker instanceof SimpleLogicModule) {
			SimpleLogicModule simpleChecker = (SimpleLogicModule) checker;
			Pair<Boolean, Entry> produceResult = eventProducer.produce(simpleChecker, result, curr);

			/*
			 * The event finishes (result = false), Context to be cleared for
			 * next events. Note that this is performed after
			 * EventProducer.produce so that context can be used to close the
			 * event
			 */
			if (!result && checker instanceof ActionLogicModule) {
				((ActionLogicModule) checker).getContext().clearContext();
			}

			if (produceResult.getLeft()) {
				results.add(produceResult.getRight());
			}
		} else {
			logger.warn("Problem postrprocessing LM results, not an instance of simple logic module");
		}
	}

	/**
	 * Run comparators for current snapshot
	 * 
	 * @param daq
	 *            current snapshot
	 * @return results of checkers analysis
	 */
	private List<Entry> runComparators(DAQ daq) {
		List<Entry> results = new ArrayList<>();
		for (ComparatorLogicModule comparator : comparators) {
			logger.trace("Running comparator " + comparator.getClass().getSimpleName());
			Date last = null;

			/* add artificial event starting point */
			if (artificialForced || comparator.getLast() == null) {
				DAQ fake = new DAQ();
				last = new Date(daq.getLastUpdate());
				fake.setLastUpdate(daq.getLastUpdate());
				comparator.setLast(fake);
			} else
				last = new Date(comparator.getLast().getLastUpdate());

			boolean result = comparator.compare(daq);
			Date current = new Date(comparator.getLast().getLastUpdate());

			// TODO: comparators may also return entry
			eventProducer.produce(comparator, result, last, current);

		}
		artificialForced = false;
		return results;
	}

	public ExperimentalProcessor getExperimentalProcessor() {
		return experimentalProcessor;
	}

	public void setArtificialForced(boolean artificialForced) {
		this.artificialForced = artificialForced;
	}
}
