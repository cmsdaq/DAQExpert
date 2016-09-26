package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class NoRate extends ExtendedCondition {

	public NoRate() {
		this.name = "No rate";
		this.group = EventGroup.NO_RATE;
		this.priority = EventPriority.defaultt;
		this.description = "Rate value is 0";
		this.action = null;
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
