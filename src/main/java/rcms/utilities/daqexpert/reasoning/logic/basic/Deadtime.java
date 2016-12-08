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
		this.setNotificationPlay(false);
	}

	/**
	 * Dead time when greater than 5%
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		double deadtime = 0;
		try {
			if (results.get(BeamActive.class.getSimpleName())) {
				deadtime = daq.getTcdsGlobalInfo().getDeadTimes().get("beamactive_total");
			} else {
				deadtime = daq.getTcdsGlobalInfo().getDeadTimes().get("total");
			}
		} catch (NullPointerException e) {
		}

		if (deadtime > 5)
			return true;
		else
			return false;
	}

}
