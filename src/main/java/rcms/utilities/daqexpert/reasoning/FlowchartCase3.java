package rcms.utilities.daqexpert.reasoning;

import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;
import rcms.utilities.daqexpert.reasoning.base.TTSState;

/**
 * Logic module identifying 3 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase3 extends ExtendedCondition {

	public FlowchartCase3() {
		this.name = "FC3";
		this.description = "FC3: Partition {{TTCP}} in {{SUBSYSTEM}} subsystem is in {{STATE}} TTS state. It's blocking trigger.";
		this.action = Arrays.asList("Issue a TTCHardReset",
				"If DAQ is still stuck after a few seconds, issue another TTCHardReset (HardReset includes a Resync, so it may be used for both OOS and ERROR)",
				"Problem fixed: Make an e-log entry",
				"Problem not fixed: Try to recover: Stop the run. Red & Green recycle the subsystem. Start a new run. Try up to 2 times",
				"Problem still not fixed after recover: Call the DOC for the partition in error/OOS",
				"Problem fixed after recover: Make an e-log entry. Call the DOC for the partition in error/OOS to inform");

		this.group = EventGroup.FL3;
		this.priority = EventPriority.critical;
	}

	private static Logger logger = Logger.getLogger(FlowchartCase3.class);

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean result = false;

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.OUT_OF_SYNC || currentState == TTSState.ERROR) {

					context.register("SUBSYSTEM", subSystem.getName());
					context.register("TTCP", subSystem.getName());
					context.register("STATE", currentState.name());
					result = true;
				}
			}
		}
		return result;
	}

}
