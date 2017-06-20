package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.LogicModuleHelper;

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
		this.name = "FED stuck";
		this.description = "TTCP {{TTCP}} of {{SUBSYSTEM}} subsystem is blocking trigger, it's in {{TTCPSTATE}} TTS state, "
				+ "The problem is caused by FED {{FED}} in {{FEDSTATE}}";

		/* default action */
		ConditionalAction action = new ConditionalAction("Stop the run",
				"Red & green recycle the subsystem {{SUBSYSTEM}}.", "Start new run (try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{SUBSYSTEM}} to inform",
				"Problem not fixed: Call the DOC for the subsystem {{SUBSYSTEM}}");

		/* ecal specific case */
		action.addContextSteps("ECAL", "Stop the run", "Start new run (try up to 2 times)",
				"Problem fixed: Make an e-log entry.", "Problem not fixed: Red recycle ECAL",
				"Call the DOC for the ECAL");

		this.action = action;
	}

	// add triggers info (behind or the same
	// number)
	// TODO: add hierarchy of FEDS (pseudo feds)
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		String daqstate = daq.getDaqState();

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

					if (!ttcp.isMasked()) {
						TTSState currentState = getParitionState(ttcp);
						if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

							Map<FED, Set<FED>> fedHierarchy = LogicModuleHelper.getFEDHierarchy(ttcp);

							for (Entry<FED, Set<FED>> fed : fedHierarchy.entrySet()) {
								TTSState currentFedState = TTSState.getByCode(fed.getKey().getTtsState());
								if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)) {

									/* there are FEDs behind the pseudo FED */
									if (fed.getValue().size() > 0) {
										for (FED dep : fed.getValue()) {
											if (dep.getPercentBackpressure() == 0F) {
												result = true;
												context.register("FED",
														"(" + dep.getSrcIdExpected() + " behind pseudo FED "
																+ fed.getKey().getSrcIdExpected() + ")");
												context.register("FEDSTATE",
														"(" + (dep.getTtsState() != null ? dep.getTtsState()
																: "FED has no individual TTS state, ")
																+ currentFedState.name() + " @ its pseudo FED)");
											}
										}
									}
									/* there are no FEDs in hierarchy */
									else {
										if (fed.getKey().getPercentBackpressure() == 0F) {
											result = true;
											context.register("FED", fed.getKey().getSrcIdExpected());
											context.register("FEDSTATE", currentFedState.name());
										}

									}

									if (result) {
										context.register("TTCP", ttcp.getName());
										context.register("TTCPSTATE", currentState.name());
										context.register("SUBSYSTEM", subSystem.getName());
										context.setActionKey(subSystem.getName());
									}
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

}
