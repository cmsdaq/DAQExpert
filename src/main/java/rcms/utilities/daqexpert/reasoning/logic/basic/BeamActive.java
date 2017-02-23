package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

/**
 * This identifies when beams are active
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class BeamActive extends SimpleLogicModule {

	public BeamActive() {
		this.name = "Beams active";
		this.description = "Beam active identified";

		this.priority = ConditionPriority.DEFAULTT;

		this.setNotificationPlay(false);
	}

	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Boolean> results) {

		LHCBeamMode currentMode = LHCBeamMode.getModeByCode(snapshot.getLhcBeamMode());
		if (currentMode == LHCBeamMode.INJECTION_PROBE_BEAM || currentMode == LHCBeamMode.INJECTION_SETUP_BEAM
				|| currentMode == LHCBeamMode.INJECTION_PHYSICS_BEAM || currentMode == LHCBeamMode.PREPARE_RAMP
				|| currentMode == LHCBeamMode.RAMP || currentMode == LHCBeamMode.FLAT_TOP
				|| currentMode == LHCBeamMode.SQUEEZE || currentMode == LHCBeamMode.ADJUST
				|| currentMode == LHCBeamMode.STABLE_BEAMS)
			return true;

		return false;
	}

}
