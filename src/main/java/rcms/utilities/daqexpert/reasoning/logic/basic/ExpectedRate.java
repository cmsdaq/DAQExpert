package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class ExpectedRate extends SimpleLogicModule {

	public ExpectedRate() {
		this.name = "Expected rate";
		this.group = EventGroup.EXPECTED_RATE;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Expecting rate";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		boolean fixingSoftError = daq.getLevelZeroState().equalsIgnoreCase("FixingSoftError") ? true : false;
		boolean dcsPauseResume = daq.getLevelZeroState().equalsIgnoreCase("PerformingDCSPauseResume") ? true : false;
		boolean pausing = daq.getLevelZeroState().equalsIgnoreCase("Pausing") ? true : false;
		boolean paused = daq.getLevelZeroState().equalsIgnoreCase("Paused") ? true : false;
		boolean resuming = daq.getLevelZeroState().equalsIgnoreCase("Resuming") ? true : false;

		if (runOngoing && !fixingSoftError && !dcsPauseResume && !pausing && !paused && !resuming)
			return true;
		return false;
	}
}
