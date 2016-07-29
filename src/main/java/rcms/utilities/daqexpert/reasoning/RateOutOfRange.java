package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class RateOutOfRange extends Condition {

	public RateOutOfRange() {
		this.name = "Rate out of range";
		this.group = EventGroup.RATE_OUT_OF_RANGE;
		this.priority = EventPriority.defaultt;
		this.description = "Rate is considered out of range when less than 50kHz";
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
