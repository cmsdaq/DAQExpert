package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 * Logic module identifying 4 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase4 extends KnownFailure {

	public FlowchartCase4() {
		this.name = "FC4";
		this.description = "TTCP {{TTCP}} in {{SUBSYSTEM}} subsystem is in disconnected TTS state. It's blocking trigger."
				+ "The PI of the subsystem may be suffering from a firmware problem";
		this.action = new SimpleAction("Stop the run", "red & green recycle the subsystem {{SUBSYSTEM}}",
				"Start new run", "Problem fixed: You are done make an e-log entry",
				"Problem not fixed: Call the DOC of {{SUBSYSTEM}} (subsystem for the partition in disconnected)");
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? ConditionPriority.CRITICAL : ConditionPriority.WARNING;

		boolean result = false;

		String daqstate = daq.getDaqState();
		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {
					if (!ttcp.isMasked()) {

						TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
						if (currentState == TTSState.DISCONNECTED) {

							context.register("SUBSYSTEM", subSystem.getName());
							context.register("TTCP", ttcp.getName());
							result = true;
						}
					}
				}
			}

		}

		return result;
	}
}
