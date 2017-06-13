package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class RunOngoing extends SimpleLogicModule {

	/**
	 * TODO: L0 or TCDS may toggle on the run ongoing
	 */
	public RunOngoing() {
		this.name = "Run ongoing";
		this.priority = ConditionPriority.IMPORTANT;
		this.description = "Run is ongoing according to TCDS state";
		this.previousState = false;
	}

	private static final List<String> levelZeroRunningStates = Arrays.asList("RUNNING", "PAUSED", "PAUSING", "RESUMING",
			"PERFORMING_DCS_PAUSE_RESUME", "FIXING_SOFT_ERROR", "TTCHARDRESETTING", "TTCRESYNCING",
			"TTCHARDRESETTINGFROMRUNNING", "TTCRESYNCINGFROMRUNNING", "RUNBLOCKED");
	private static final List<String> levelZeroNotRunningStates = Arrays.asList("INITIAL", "INITIALIZED", "HALTED",
			"CONFIGURED", "INITIALIZING", "CONNECTING", "CONFIGURING", "HALTING", "STARTING", "RESUME_STARTING",
			"RESETTING", "STOPPING", "FORCESTOPPING", "FORCEHALTING", "TESTING_TTS", "PREPARING_TTSTEST_MODE",
			"COLDRESETTING");
	private static final List<String> levelZeroNotSureStates = Arrays.asList("ERROR", "FATALERROR", "UNDEFINED",
			"RECOVERING");

	private static final List<String> tcdsRunningStates = Arrays.asList("RUNNING", "RUNNINGDEGRADED",
			"RUNNINGSOFTERRORDETECTED", "RESUMING", "FIXINGSOFTERROR", "TTCRESYNCING", "TTCHARDRESETTING",
			"TTCRESYNCINGFROMRUNNING", "TTCHARDRESETTINGFROMRUNNING");
	private static final List<String> tcdsNotRunningStates = Arrays.asList("INITIAL", "HALTED", "CONFIGURED",
			"TTSTEST_MODE", "INITIALIZING", "CONFIGURING", "HALTING", "STARTING", "STOPPING", "RESETTING",
			"TESTING_TTS", "PREPARING_TTSTEST_MODE", "COLDRESETTING");
	private static final List<String> tcdsNotSureStates = Arrays.asList("PAUSED", "ERROR", "PAUSING", "RECOVERING");

	private boolean previousState;

	private static final Logger logger = Logger.getLogger(RunOngoing.class);

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		String tcdsState = "", levelZeroState;

		for (SubSystem curr : daq.getSubSystems()) {

			/* check tcds subsystem state */
			if (curr.getName().equalsIgnoreCase("TCDS")) {// change to constant
				tcdsState = curr.getStatus();
			}
		}
		levelZeroState = daq.getLevelZeroState();

		boolean currentState = determineRunOngoing(tcdsState, levelZeroState);

		this.previousState = currentState;
		return currentState;
	}

	public boolean determineRunOngoing(String tcdsState, String levelZeroState) {
		if (levelZeroRunningStates.contains(levelZeroState) || tcdsRunningStates.contains(tcdsState)) {
			logger.info("At least one of them has 'runnning' state: L0=" + levelZeroState + ", TCDS=" + tcdsState);
			return true;
		}

		if (levelZeroNotRunningStates.contains(levelZeroState) || tcdsNotRunningStates.contains(tcdsState)) {
			logger.info("At least one of them has 'runnning' state: L0=" + levelZeroState + ", TCDS=" + tcdsState);
			return false;
		}

		if (levelZeroNotSureStates.contains(levelZeroState) && tcdsNotSureStates.contains(tcdsState)) {
			logger.info("Both L0 and TCDS has a state from 'not sure', keeping the previous value: L0=" + levelZeroState
					+ ", TCDS=" + tcdsState);
			return previousState;
		}

		return previousState;
	}
}
