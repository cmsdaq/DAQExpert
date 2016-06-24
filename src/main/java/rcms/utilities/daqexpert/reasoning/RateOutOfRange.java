package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class RateOutOfRange extends Condition {

	public RateOutOfRange() {
		this.name = "Rate out of range";
		this.group = EventGroup.Info;
		this.priority = EventPriority.defaultt;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		float a = daq.getFedBuilderSummary().getRate();

		boolean result = false;
		if (50000 > a)
			result = true;

		return result;
	}

}
