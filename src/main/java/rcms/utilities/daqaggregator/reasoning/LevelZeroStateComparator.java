package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class LevelZeroStateComparator extends Comparator {

	private static Logger logger = Logger.getLogger(LevelZeroStateComparator.class);

	private String runId;

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLevelZeroState().equals(previous.getLevelZeroState())) {
			logger.debug("New L0 state " + current.getLevelZeroState());
			runId = "L0 state: " + current.getLevelZeroState();
			result = true;
		}
		return result;
	}

	@Override
	public String getText() {
		return runId;
	}

	@Override
	public Level getLevel() {
		return Level.DAQ;
	}
	
	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}

}
