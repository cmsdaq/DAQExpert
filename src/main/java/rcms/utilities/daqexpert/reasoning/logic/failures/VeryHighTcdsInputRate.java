package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TCDSTriggerRates;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

/** This module fires in for very high TCDS trigger input rate.
 */
public class VeryHighTcdsInputRate extends KnownFailure implements Parameterizable {

	/** minimum above which this module fires */
	private float threshold;

	public VeryHighTcdsInputRate() {
		this.name = "Very high trigger input rate";
		this.threshold = 0;

		this.description = "failed to set description";
		this.action = new SimpleAction("Ask the trigger shifter to check the inputs to the L1 trigger (noisy towers, failed links)",
		                               "Make an e-log entry"
		                               );
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		// assign the priority based on whether we are in stable beams or not
		assignPriority(results);

		TCDSTriggerRates rates = daq.getTcdsGlobalInfo().getTriggerRatesInstant();

		double inputTriggerRate = rates.getTrg_rate_total() + rates.getSup_trg_rate_total();

		boolean result = false;
		if (threshold < inputTriggerRate) {
			context.registerForStatistics("TCDS_TRIGGER_INPUT_RATE", inputTriggerRate,"Hz",1);
			result = true;
		}
		return result;
	}

	@Override
	public void parametrize(Properties properties) {

		try {
			this.threshold = Integer.parseInt(properties.getProperty(Setting.EXPERT_TCDS_INPUT_RATE_VERYHIGH.getKey()));
			this.description = "The TCDS trigger input rate is {{TCDS_TRIGGER_INPUT_RATE}} " +
							"which is very high (above " + threshold + " Hz). " +
							"This may be a problem with the L1 trigger: noisy towers, failed links etc.";

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}
	}

}
