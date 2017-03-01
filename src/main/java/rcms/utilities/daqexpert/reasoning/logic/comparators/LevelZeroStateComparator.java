package rcms.utilities.daqexpert.reasoning.logic.comparators;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class LevelZeroStateComparator extends ComparatorLogicModule {

	public LevelZeroStateComparator() {
		this.name = "n/a";
		this.priority = ConditionPriority.DEFAULTT;
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
