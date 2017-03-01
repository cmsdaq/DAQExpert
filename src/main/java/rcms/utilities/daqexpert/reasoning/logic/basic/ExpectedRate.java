package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class ExpectedRate extends SimpleLogicModule {

	public ExpectedRate() {
		this.name = "Expected rate";
		this.priority = ConditionPriority.DEFAULTT;
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
		boolean ttcHardResettingFromRunning = daq.getLevelZeroState().equalsIgnoreCase("TTCHardResettingFromRunning")
				? true : false;
		boolean ttcResyncingFromRunning = daq.getLevelZeroState().equalsIgnoreCase("TTCResyncingFromRunning")
				? true : false;
		

		if (runOngoing && !fixingSoftError && !dcsPauseResume && !pausing && !paused && !resuming
				&& !ttcHardResettingFromRunning && !ttcResyncingFromRunning)
			return true;
		return false;
	}
}
