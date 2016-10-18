package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

public class NoRateWhenExpected extends SimpleLogicModule {

	public NoRateWhenExpected() {
		this.name = "No rate when expected";
		this.group = EventGroup.NO_RATE_WHEN_EXPECTED;
		this.priority = EventPriority.CRITICAL;
		this.description = "No rate when expected";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		boolean stableBeams = false;
		boolean runOngoing = false;
		boolean noRate = false;

		stableBeams = results.get(StableBeams.class.getSimpleName());
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
