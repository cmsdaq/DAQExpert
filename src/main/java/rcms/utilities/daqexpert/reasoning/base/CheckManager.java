package rcms.utilities.daqexpert.reasoning.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.AvoidableDowntime;
import rcms.utilities.daqexpert.reasoning.DAQStateComparator;
import rcms.utilities.daqexpert.reasoning.Downtime;
import rcms.utilities.daqexpert.reasoning.EVMComparator;
import rcms.utilities.daqexpert.reasoning.FlowchartCase1;
import rcms.utilities.daqexpert.reasoning.FlowchartCase2;
import rcms.utilities.daqexpert.reasoning.FlowchartCase3;
import rcms.utilities.daqexpert.reasoning.FlowchartCase4;
import rcms.utilities.daqexpert.reasoning.FlowchartCase5;
import rcms.utilities.daqexpert.reasoning.LHCBeamModeComparator;
import rcms.utilities.daqexpert.reasoning.LHCMachineModeComparator;
import rcms.utilities.daqexpert.reasoning.LevelZeroStateComparator;
import rcms.utilities.daqexpert.reasoning.NoRate;
import rcms.utilities.daqexpert.reasoning.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.RateOutOfRange;
import rcms.utilities.daqexpert.reasoning.RunComparator;
import rcms.utilities.daqexpert.reasoning.RunOngoing;
import rcms.utilities.daqexpert.reasoning.SessionComparator;
import rcms.utilities.daqexpert.reasoning.WarningInSubsystem;

/**
 * Manager of checking process
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class CheckManager {

	private final static Logger logger = Logger.getLogger(CheckManager.class);
	private final List<Condition> checkers = new ArrayList<>();

	private final List<Comparator> comparators = new ArrayList<>();

	/**
	 * Constructor, order of checker matters. Checkers may use results of
	 * checkers added before.
	 * 
	 * @param daq
	 *            daq object to analyze
	 */
	public CheckManager() {
		// Level 0 Independent
		checkers.add(new RateOutOfRange());
		checkers.add(new NoRate());
		checkers.add(new RunOngoing());
		checkers.add(new WarningInSubsystem());

		// Level 1 (depends on L0)
		checkers.add(new NoRateWhenExpected());
		checkers.add(new Downtime());
		checkers.add(new AvoidableDowntime());

		// Level 2 (depends on L1)
		checkers.add(new FlowchartCase1());
		checkers.add(new FlowchartCase2());
		checkers.add(new FlowchartCase3());
		checkers.add(new FlowchartCase4());
		checkers.add(new FlowchartCase5());
		//checkers.add(new FlowchartCase6());

		// comparators
		comparators.add(new SessionComparator());
		comparators.add(new LHCBeamModeComparator());
		comparators.add(new LHCMachineModeComparator());
		comparators.add(new RunComparator());
		comparators.add(new LevelZeroStateComparator());
		comparators.add(new DAQStateComparator());
		comparators.add(new EVMComparator());
	}

	/**
	 * Run all logic modules for current snapshot
	 * 
	 * @param daq
	 *            current snapshot
	 * @return results of logic modules analysis
	 */
	public List<Entry> runLogicModules(DAQ daq) {

		List<Entry> results = new ArrayList<>();

		logger.debug("Running analysis modules for run " + daq.getSessionId());

		results.addAll(runCheckers(daq));
		results.addAll(runComparators(daq));

		return results;
	}

	/**
	 * Run checkers for current snapshot
	 * 
	 * @param daq
	 *            current snapshot
	 * @return results of checkers analysis
	 */
	private List<Entry> runCheckers(DAQ daq) {
		List<Entry> results = new ArrayList<>();
		Date curr = null;
		HashMap<String, Boolean> checkerResultMap = new HashMap<>();

		for (Condition checker : checkers) {
			boolean result = checker.satisfied(daq, checkerResultMap);

			checkerResultMap.put(checker.getClass().getSimpleName(), result);
			curr = new Date(daq.getLastUpdate());
			Entry entry = EventProducer.get().produce(checker, result, curr);

			/*
			 * The event finishes (result = false), Context to be cleared for
			 * next events. Note that this is performed after
			 * EventProducer.produce so that context can be used to close the
			 * event
			 */
			if (!result && checker instanceof ExtendedCondition) {
				((ExtendedCondition) checker).context.clearContext();
			}

			if (entry != null) {
				results.add(entry);
			}
		}
		results.addAll(EventProducer.get().getFinishedThisRound());
		EventProducer.get().clearFinishedThisRound();

		return results;
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
		for (Comparator comparator : comparators) {
			Date last = null;
			if (comparator.getLast() != null)
				last = new Date(comparator.getLast().getLastUpdate());

			/* add artificial event starting point */
			if (last == null) {
				DAQ fake = new DAQ();
				last = new Date(daq.getLastUpdate());
				fake.setLastUpdate(daq.getLastUpdate());
				comparator.setLast(fake);
			}

			boolean result = comparator.compare(daq);
			Date current = new Date(comparator.getLast().getLastUpdate());

			// TODO: comparators may also return entry
			EventProducer.get().produce(comparator, result, last, current);
		}
		return results;
	}
}
