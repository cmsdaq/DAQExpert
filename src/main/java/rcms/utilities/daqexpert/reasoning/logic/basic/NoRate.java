package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class NoRate extends SimpleLogicModule {

	public NoRate() {
		this.name = "No rate";
		this.group = EventGroup.NO_RATE;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Rate value is 0";
	}

	/**
	 * No rate when sum of FedBuilders rate equals 0 Hz
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		if (daq.getFedBuilderSummary().getRate() == 0)
			return true;
		else
			return false;
	}

}
