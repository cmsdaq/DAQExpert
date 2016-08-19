package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;
import rcms.utilities.daqexpert.reasoning.states.LHCBeamMode;

public class NoRateWhenExpected extends ExtendedCondition {

	public NoRateWhenExpected() {
		this.name = "No rate when expected";
		this.group = EventGroup.NO_RATE_WHEN_EXPECTED;
		this.priority = EventPriority.critical;
		this.description = "No rate when expected";
		this.action = null;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		boolean stableBeams = false;
		boolean runOngoing = false;
		boolean noRate = false;
		if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(daq.getLhcBeamMode()))
			stableBeams = true;
		runOngoing = results.get(RunOngoing.class.getSimpleName());
		noRate = results.get(NoRate.class.getSimpleName());

		boolean fixingSoftError = daq.getLevelZeroState().equalsIgnoreCase("FixingSoftError") ? true : false;
		boolean dcsPauseResume = daq.getLevelZeroState().equalsIgnoreCase("PerformingDCSPauseResume") ? true : false;
		// TODO: ! tcds paused + pausing + resuming

		if (stableBeams && runOngoing && noRate && !fixingSoftError && !dcsPauseResume)
			return true;
		return false;
	}

}
