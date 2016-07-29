package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class NoRate extends Condition {

	public NoRate() {
		this.name = "No rate";
		this.group = EventGroup.NO_RATE;
		this.priority = EventPriority.defaultt;
		this.description = "Rate value is 0";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		float rate = daq.getFedBuilderSummary().getRate();
		if (rate == 0)
			return true;
		return false;
	}

}
