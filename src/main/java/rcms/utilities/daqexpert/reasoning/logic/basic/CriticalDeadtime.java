package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies deadtime
 */
public class CriticalDeadtime extends SimpleLogicModule {

	public CriticalDeadtime() {
		this.name = "Critical deadtime";
		this.group = EventGroup.CRITICAL_DEADTIME;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Deadtime is greater than 5% during running";
		this.setNotificationPlay(true);
		this.setSoundToPlay(Sound.DEADTIME);
	}

	/**
	 * Dead time when greater than 5%
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean deadtime = false;
		boolean transition = false;
		boolean expectedRate = false;

		deadtime = results.get(Deadtime.class.getSimpleName());
		expectedRate = results.get(ExpectedRate.class.getSimpleName());
		transition = results.get(LongTransition.class.getSimpleName());

		if (deadtime && expectedRate && !transition) {
			return true;
		}

		return false;
	}

}
