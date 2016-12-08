package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Iterator;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies individual FED deadtime
 */
public class FEDDeadtime extends ActionLogicModule {

	private final float threshold;

	public FEDDeadtime(final float threshold) {
		this.name = "FED deadtime";
		this.group = EventGroup.FED_DEADTIME;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Deadtime of fed(s) {{FED}} in subsystem(s) {{SUBSYSTEM}} is greater than 5%";
		this.setNotificationPlay(true);
		this.threshold = threshold;
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

}
