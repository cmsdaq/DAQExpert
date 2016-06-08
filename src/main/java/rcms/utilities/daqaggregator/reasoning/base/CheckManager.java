package rcms.utilities.daqaggregator.reasoning.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.DAQStateComparator;
import rcms.utilities.daqaggregator.reasoning.EVMComparator;
import rcms.utilities.daqaggregator.reasoning.LHCBeamModeComparator;
import rcms.utilities.daqaggregator.reasoning.LHCMachineModeComparator;
import rcms.utilities.daqaggregator.reasoning.LevelZeroStateComparator;
import rcms.utilities.daqaggregator.reasoning.Message1;
import rcms.utilities.daqaggregator.reasoning.Message2;
import rcms.utilities.daqaggregator.reasoning.Message3;
import rcms.utilities.daqaggregator.reasoning.Message4;
import rcms.utilities.daqaggregator.reasoning.NoRate;
import rcms.utilities.daqaggregator.reasoning.RateOutOfRange;
import rcms.utilities.daqaggregator.reasoning.RunComparator;
import rcms.utilities.daqaggregator.reasoning.SessionComparator;
import rcms.utilities.daqaggregator.reasoning.WarningInSubsystem;

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
		checkers.add(new RateOutOfRange());
		checkers.add(new NoRate());
		checkers.add(new WarningInSubsystem());
		checkers.add(new Message1());
		checkers.add(new Message2());
		checkers.add(new Message3());
		checkers.add(new Message4());
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
		for (Condition checker : checkers) {
			boolean result = checker.satisfied(daq);
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
