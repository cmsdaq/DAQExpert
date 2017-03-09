package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies deadtime
 */
public class CriticalDeadtime extends SimpleLogicModule {

	public CriticalDeadtime() {
		this.name = "Critical deadtime";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "There is deadtime during running";
		this.setNotificationPlay(true);
		this.setSoundToPlay(Sound.DEADTIME);
	}

	/**
	 * Dead time during running
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
