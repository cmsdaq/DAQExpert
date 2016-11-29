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
public class Deadtime extends SimpleLogicModule {

	public Deadtime() {
		this.name = "Deadtime";
		this.group = EventGroup.DEADTIME;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Deadtime is greater than 5%";
		this.setNotificationPlay(true);// TODO: make it true
		this.setSoundToPlay(Sound.DEADTIME);
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

		if (daq.getTcdsGlobalInfo().getDeadTimes().get("total") > 5)
			return true;
		else
			return false;
	}

}
