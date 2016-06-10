package rcms.utilities.daqexpert.reasoning.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.DAQStateComparator;
import rcms.utilities.daqexpert.reasoning.EVMComparator;
import rcms.utilities.daqexpert.reasoning.LHCBeamModeComparator;
import rcms.utilities.daqexpert.reasoning.LHCMachineModeComparator;
import rcms.utilities.daqexpert.reasoning.LevelZeroStateComparator;
import rcms.utilities.daqexpert.reasoning.Message1;
import rcms.utilities.daqexpert.reasoning.Message2;
import rcms.utilities.daqexpert.reasoning.Message3;
import rcms.utilities.daqexpert.reasoning.Message4;
import rcms.utilities.daqexpert.reasoning.Message5;
import rcms.utilities.daqexpert.reasoning.Message6;
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
	 * Constructor
	 * 
	 * @param daq
	 *            daq object to analyze
	 */
	public CheckManager() {
		// Level 0  Independent
		checkers.add(new RateOutOfRange());
		checkers.add(new NoRate());
		checkers.add(new RunOngoing());
		checkers.add(new WarningInSubsystem());

		// Level 1 (depends on L0)
		checkers.add(new NoRateWhenExpected());

		// Level 2 (depends on L1)
		checkers.add(new Message1());
		checkers.add(new Message2());
		checkers.add(new Message3());
		checkers.add(new Message4());
		checkers.add(new Message5());
		checkers.add(new Message6());

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
	 * Run all checkers
	 */
	public void runCheckers(DAQ daq) {

		logger.debug("Running analysis modules for run " + daq.getSessionId());
		Date curr = null;
		HashMap<String, Boolean> results = new HashMap<>();

		for (Condition checker : checkers) {
			if (checker instanceof Aware) {
				((Aware) checker).setResults(results);
				// System.out.println("Aware checker: " +
				// checker.getClass().getSimpleName());
			}
		}

		for (Condition checker : checkers) {
			boolean result = checker.satisfied(daq);
			results.put(checker.getClass().getSimpleName(), result);
			curr = new Date(daq.getLastUpdate());
			Entry entry = EventProducer.get().produce(checker, result, curr);
			if (entry != null && result)
				checker.gatherInfo(daq, entry);
		}
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

			EventProducer.get().produce(comparator, result, last, current);
		}
	}
}
