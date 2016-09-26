package rcms.utilities.daqexpert.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class LevelZeroStateComparator extends Comparator {

	public LevelZeroStateComparator() {
		this.name = "n/a";
		this.group = EventGroup.LEVEL_ZERO;
		this.priority = EventPriority.defaultt;
		this.description = "New Level zero state identified";
	}

	private static Logger logger = Logger.getLogger(LevelZeroStateComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLevelZeroState().equals(previous.getLevelZeroState())) {
			logger.debug("New L0 state " + current.getLevelZeroState());
			this.name = current.getLevelZeroState();
			result = true;
		}
		return result;
	}

}
