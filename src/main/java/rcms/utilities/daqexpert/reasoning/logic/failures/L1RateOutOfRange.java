package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class L1RateOutOfRange extends ActionLogicModule implements Parameterizable {

	private static final Logger logger = Logger.getLogger(L1RateOutOfRange.class);
	private float min;
	private float max;

	public L1RateOutOfRange() {

		this.name = "L1 rate out of range";

		this.description = null; // set in parametrixe method

		this.action = new SimpleAction(
				"Check the individual level 1 rates or ask for them to be chcecked by the trigger shifter");

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		assignPriority(results);

		double supTrgRateTotal = daq.getTcdsGlobalInfo().getSup_trg_rate_total();
		double trgRateTotal = daq.getTcdsGlobalInfo().getTrg_rate_total();

		double rate = supTrgRateTotal + trgRateTotal;


		boolean result = false;
		if (min > rate || max < rate){
			logger.debug("Out of range L1 trigger rate: " + rate);
			result = true;
		}
		return result;

	}

	@Override
	public void parametrize(Properties properties) {

		try {
			this.min = Integer.parseInt(properties.getProperty(Setting.EXPERT_L1_TRIGGER_MIN.getKey()));
			this.max = Integer.parseInt(properties.getProperty(Setting.EXPERT_L1_TRIGGER_MAX.getKey()));
			this.description = "L1 trigger rate out of expected range [" + min + "; " + max + "]";

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}
	}
}
