package rcms.utilities.daqexpert.reasoning.logic.comparators;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class RunComparator extends ComparatorLogicModule {

	public RunComparator() {
		this.name = "n/a";
		this.group = EventGroup.RUN_NUMBER;
		this.priority = EventPriority.defaultt;
		this.description = "New run has been identified";
	}

	private static Logger logger = Logger.getLogger(RunComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (current.getRunNumber() != previous.getRunNumber()) {
			logger.debug("New run identified " + current.getRunNumber());
			this.name = "" + current.getRunNumber();
			result = true;
		}
		return result;
	}

}
