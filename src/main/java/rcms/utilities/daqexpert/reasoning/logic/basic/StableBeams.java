package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

/**
 * This identifies when stable beams is on
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class StableBeams extends SimpleLogicModule {

	public StableBeams() {
		this.name = "Stable beams";
		this.description = "Stable beams identified";

		this.group = EventGroup.HIDDEN;
		this.priority = EventPriority.defaultt;
	}

	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Boolean> results) {

		if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(snapshot.getLhcBeamMode()))
			return true;
		return false;
	}

}
