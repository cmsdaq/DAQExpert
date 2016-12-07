package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class RateOutOfRange extends SimpleLogicModule {

	private final float min;
	private final float max;

	public RateOutOfRange(float min, float max) {
		this.name = "Rate out of range";
		this.group = EventGroup.RATE_OUT_OF_RANGE;
		this.priority = EventPriority.DEFAULTT;
		this.description = "L1 rate out of expected range [" + min + "; " + max + "]";
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		float a = daq.getFedBuilderSummary().getRate();

		boolean result = false;
		if (min > a || max < a)
			result = true;

		return result;
	}

}
