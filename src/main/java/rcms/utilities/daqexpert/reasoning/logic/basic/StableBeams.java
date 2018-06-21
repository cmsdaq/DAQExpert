package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
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
		this.priority = ConditionPriority.DEFAULTT;
		this.problematic = false;
	}

	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Output> results) {

		if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(snapshot.getLhcBeamMode()))
			return true;
		return false;
	}

}
