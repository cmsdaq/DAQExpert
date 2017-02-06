package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 * Logic module identifying 5 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase5 extends KnownFailure {

	public FlowchartCase5() {
		this.name = "FC5";
		this.description = "TTCP {{TTCP}} of {{SUBSYSTEM}} subsystem is blocking trigger, it's in {{TTCPSTATE}} TTS state, "
				+ "The problem is caused by FED {{FED}} in {{FEDSTATE}}";

		/* default action */
		ConditionalAction action = new ConditionalAction("Stop the run",
				"Red & green recycle the subsystem {{SUBSYSTEM}}.", "Start new run (try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{SUBSYSTEM}} to inform",
				"Problem not fixed: Call the DOC for the subsystem {{SUBSYSTEM}}");

		/* ecal specific case */
		action.addContextSteps("ECAL", "Stop the run", "Start new run (try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{SUBSYSTEM}} to inform",
				"Problem not fixed: Call the DOC for the subsystem {{SUBSYSTEM}}");

		this.action = action;
	}

	private static final String RUNBLOCKED_STATE = "RUNBLOCKED";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? EventPriority.CRITICAL : EventPriority.DEFAULTT;

		boolean result = false;

		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

					TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
					if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

						for (FED fed : ttcp.getFeds()) {
							TTSState currentFedState = TTSState.getByCode(fed.getTtsState());
							if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)
									&& fed.getPercentBackpressure() == 0F) {

								context.register("TTCP", ttcp.getName());
								context.register("TTCPSTATE", currentState.name());
								context.register("SUBSYSTEM", subSystem.getName());
								context.register("FED", fed.getSrcIdExpected());
								context.register("FEDSTATE", currentFedState.name());
								context.setActionKey(subSystem.getName());
								result = true;
							}
						}
					}
				}
			}
		}

		return result;
	}
}
