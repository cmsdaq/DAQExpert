package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying 4 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase4 extends ExtendedCondition {

	public FlowchartCase4() {
		this.name = "FC4";
		this.description = "TTCP {{TTCP}} in {{SUBSYSTEM}} subsystem is in disconnected TTS state. It's blocking trigger."
				+ "The PI of the subsystem may be suffering from a firmware problem";
		this.action = new SimpleAction("Stop the run", "red & green recycle the subsystem {{SUBSYSTEM}}",
				"Start new run", "Problem fixed: You are done make an e-log entry",
				"Problem not fixed: Call the DOC of {{SUBSYSTEM}} (subsystem for the partition in disconnected)");
		this.group = EventGroup.FLOWCHART;
		this.priority = EventPriority.critical;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		boolean result = false;
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.DISCONNECTED) {

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
