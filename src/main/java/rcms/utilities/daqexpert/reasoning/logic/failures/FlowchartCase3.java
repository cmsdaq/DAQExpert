package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 * Logic module identifying 3 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase3 extends KnownFailure {

	public FlowchartCase3() {
		this.name = "FC3";
		this.description = "Partition {{TTCP}} in {{SUBSYSTEM}} subsystem is in {{STATE}} TTS state. It's blocking trigger.";
		this.action = new SimpleAction("Issue a TTCHardReset",
				"If DAQ is still stuck after a few seconds, issue another TTCHardReset (HardReset includes a Resync, so it may be used for both OOS and ERROR)",
				"Problem fixed: Make an e-log entry",
				"Problem not fixed: Try to recover: Stop the run. Red & Green recycle the subsystem {{SUBSYSTEM}}. Start a new run. Try up to 2 times",
				"Problem still not fixed after recover: Call the DOC of {{SUBSYSTEM}} (for the partition in {{STATE}})",
				"Problem fixed after recover: Make an e-log entry. Call the DOC of {{SUBSYSTEM}} (for the partition in {{STATE}}) to inform");

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? EventPriority.CRITICAL : EventPriority.DEFAULTT;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.OUT_OF_SYNC || currentState == TTSState.ERROR) {

					context.register("SUBSYSTEM", subSystem.getName());
					context.register("TTCP", ttcp.getName());
					context.register("STATE", currentState.name());
					result = true;
				}
			}
		}
		return result;
	}

}
