package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class NoRate extends SimpleLogicModule {

	public NoRate() {
		this.name = "No rate";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "Rate value is 0";
		this.problematic = false;
	}

	/**
	 * No rate when sum of FedBuilders rate equals 0 Hz
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {
		if (daq.getFedBuilderSummary().getRate() == 0)
			return true;
		else
			return false;
	}

}
