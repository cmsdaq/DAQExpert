package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class RateOutOfRange extends SimpleLogicModule implements Parameterizable {

	private float min;
	private float max;

	public RateOutOfRange() {
		this.name = "Rate out of range";
		this.priority = ConditionPriority.DEFAULTT;
		this.min = 0;
		this.max = 0;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {
		float a = daq.getFedBuilderSummary().getRate();

		boolean result = false;
		if (min > a || max < a)
			result = true;

		return result;
	}

	@Override
	public void parametrize(Properties properties) {

		try {
			this.min = Integer.parseInt(properties.getProperty(Setting.EXPERT_L1_RATE_MIN.getKey()));
			this.max = Integer.parseInt(properties.getProperty(Setting.EXPERT_L1_RATE_MAX.getKey()));
			this.description = "L1 rate out of expected range [" + min + "; " + max + "]";

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}
	}

}
