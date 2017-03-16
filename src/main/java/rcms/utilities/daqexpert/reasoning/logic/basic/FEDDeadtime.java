package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies individual FED deadtime
 */
public class FEDDeadtime extends ContextLogicModule implements Parameterizable {

	private float threshold;

	public FEDDeadtime() {
		this.name = "FED deadtime";
		this.priority = ConditionPriority.DEFAULTT;
		this.threshold = 0;
	}

	/**
	 * Dead time when greater than 5%
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean transition = false;
		boolean expectedRate = false;
		expectedRate = results.get(ExpectedRate.class.getSimpleName());
		if (!expectedRate)
			return false;
		transition = results.get(LongTransition.class.getSimpleName());
		if (transition)
			return false;

		boolean result = false;

		Iterator<FED> i = daq.getFeds().iterator();

		while (i.hasNext()) {
			FED fed = i.next();
			if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
				float deadPercentage = 0;
				deadPercentage += fed.getPercentBackpressure();
				deadPercentage += fed.getPercentBusy();
				deadPercentage += fed.getPercentWarning();

				if (deadPercentage > threshold) {
					result = true;
					context.register("FED", fed.getSrcIdExpected());
					context.register("SUBSYSTEM", fed.getTtcp().getSubsystem().getName());
				}
			}
		}

		return result;
	}

	@Override
	public void parametrize(Properties properties) {
		try {
			this.threshold = Integer
					.parseInt(properties.getProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey()));

			this.description = "Deadtime of fed(s) {{FED}} in subsystem(s) {{SUBSYSTEM}} is greater than " + threshold
					+ "%";
		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}

	}

}
