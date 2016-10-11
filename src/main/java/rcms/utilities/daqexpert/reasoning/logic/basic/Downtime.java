package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

/**
 * This logic module identifies avoidable downtime condition in DAQ
 */
public class Downtime extends ActionLogicModule {

	public Downtime() {
		this.name = "Downtime";
		this.group = EventGroup.DOWNTIME;
		this.priority = EventPriority.warning;
		this.description = "No rate during stable beams";
		this.action = null;
	}

	/**
	 * Avoidable downtime when downtime and no action being executed
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean stableBeams = false;
		boolean noRate = false;
		if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(daq.getLhcBeamMode()))
			stableBeams = true;
		noRate = results.get(NoRate.class.getSimpleName());

		if (stableBeams && noRate)
			return true;
		else
			return false;
	}

}
