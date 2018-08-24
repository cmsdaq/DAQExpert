package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class NoRate extends SimpleLogicModule implements Parameterizable {

	/** The threshold below which the trigger rate is considered as zero.
	 *  With normal running conditions this value should be set to zero
	 *  but are special running conditions (such as splashes) when
	 *  even periodic calibration triggers are disabled and zero trigger
	 *  rate is not necessarily an indication of a problem with data taking. 
	 */
	private float threshold;
	
	public NoRate() {
		this.name = "No rate";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "Rate value is 0";
		this.briefDescription = "Trigger rate is 0 Hz";
		this.problematic = false;
	}

	/**
	 * No rate when sum of FedBuilders rate equals 0 Hz
	 */
	@Override
	public boolean satisfied(DAQ daq) {
		if (daq.getFedBuilderSummary().getRate() <= threshold)
			return true;
		else
			return false;
	}

	@Override
	public void parametrize(Properties properties) {
		this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_NO_TRIGGER_RATE_THRESHOLD, this.getClass());
	}

}
