package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

/** This class is similar to RateOutOfRange but is intended as a
 *  hotfix to display a warning and action to the DAQ shifter
 *  when the readout rate exceeds the expected maximum.
 */
public class RateTooHigh extends KnownFailure implements Parameterizable {

	/** upper end of range for expected readout rate */
	private float max;

	public RateTooHigh() {
		this.name = "Readout rate too high";
		this.max = 0;

		this.description = "failed to set description";
		this.briefDescription = "The trigger rate of {{ACTUAL_READOUT_RATE}} is to high";
    	this.action = new SimpleAction("Ask the trigger shifter to check the inputs to the L1 trigger",
                                   "Make an e-log entry"
			);
	}

	@Override
	public void declareRelations(){
		declareCause(LogicModuleRegistry.HighTcdsInputRate);
		declareCause(LogicModuleRegistry.VeryHighTcdsInputRate);

		declareAffected(LogicModuleRegistry.HltOutputBandwidthTooHigh);
		declareAffected(LogicModuleRegistry.HltOutputBandwidthExtreme);

		declareAffected(LogicModuleRegistry.HltCpuLoad);
		declareAffected(LogicModuleRegistry.TTSDeadtime);
		declareAffected(LogicModuleRegistry.FedGeneratesDeadtime);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		// assign the priority based on whether we are in stable beams or not
		assignPriority(results);

		float readoutRate = daq.getFedBuilderSummary().getRate();

		boolean result = false;
		if (max < readoutRate) {
			contextHandler.registerForStatistics("ACTUAL_READOUT_RATE", readoutRate,"Hz",1);
			result = true;
		}
		return result;
	}

	@Override
	public void parametrize(Properties properties) {
		this.max = FailFastParameterReader.getIntegerParameter(properties,Setting.EXPERT_L1_RATE_MAX, this.getClass());
		this.description = "The readout rate is {{ACTUAL_READOUT_RATE}} which is above the expected maximum " + max + " Hz. This may be a problem with the L1 trigger.";
	}

}
