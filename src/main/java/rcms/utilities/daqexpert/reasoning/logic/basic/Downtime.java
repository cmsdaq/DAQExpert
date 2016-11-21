package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies avoidable downtime condition in DAQ
 */
public class Downtime extends ActionLogicModule {

	public Downtime() {
		this.name = "Downtime";
		this.group = EventGroup.DOWNTIME;
		this.priority = EventPriority.WARNING;
		this.description = "No rate during stable beams";
		this.action = null;
	}

	/**
	 * Avoidable downtime when downtime and no action being executed
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean noRate = results.get(NoRate.class.getSimpleName());
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? EventPriority.WARNING : EventPriority.DEFAULTT;

		if (stableBeams && noRate)
			return true;
		else
			return false;
	}

}
