package rcms.utilities.daqexpert.reasoning.logic.comparators;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class DAQStateComparator extends ComparatorLogicModule {

	public DAQStateComparator() {
		this.name = "n/a";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "New DAQ state identified";
	}

	private static Logger logger = Logger.getLogger(DAQStateComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getDaqState().equals(previous.getDaqState())) {
			logger.debug("DAQ state " + current.getDaqState());
			this.name = current.getDaqState();
			result = true;
		}
		return result;
	}

}
