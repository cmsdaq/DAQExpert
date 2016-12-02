package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies individual FED deadtime
 */
public class FEDDeadtime extends SimpleLogicModule {

	public FEDDeadtime() {
		this.name = "FED deadtime";
		this.group = EventGroup.Warning;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Deadtime of fed is greater than ?%";
	}

	/**
	 * Dead time when greater than ?%
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
		
		//daq.getFeds().iterator().next().

		return false;
	}

}
