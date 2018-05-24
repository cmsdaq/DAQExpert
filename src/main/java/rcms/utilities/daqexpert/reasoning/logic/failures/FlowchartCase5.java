package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

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

		/* tracker specific case only when warning state */
		action.addContextSteps("TRACKER-WARNING", "Issue a TTCHardReset once", "Problem fixed: Make an e-log entry.",
				"Problem not fixed: Stop the run", "Problem still not fixed: Red recycle TRACKER",
				"Call the DOC for the TRACKER");

		this.action = action;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);

		declareAffected(LogicModuleRegistry.TTSDeadtime);
		declareAffected(LogicModuleRegistry.FlowchartCase3);
		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	// add triggers info (behind or the same
	// number)
	// TODO: add hierarchy of FEDS (pseudo feds)
	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
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

							Map<FED, Set<FED>> fedHierarchy = FEDHierarchyRetriever.getFEDHierarchy(ttcp);
							boolean existsAtLeaseOneFedBackpressured = false;

							for (Entry<FED, Set<FED>> fed : fedHierarchy.entrySet()) {
								TTSState currentFedState = TTSState.getByCode(fed.getKey().getTtsState());
								if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)) {

									/* there are FEDs behind the pseudo FED */
									if (fed.getValue().size() > 0) {
										for (FED dep : fed.getValue()) {
											if (dep.getPercentBackpressure() == 0F) {
												result = true;
												contextHandler.register("FED",
														"(" + dep.getSrcIdExpected() + " behind pseudo FED "
																+ fed.getKey().getSrcIdExpected() + ")");
												contextHandler.register("FEDSTATE",
														"(" + (dep.getTtsState() != null ? dep.getTtsState()
																: "FED has no individual TTS state, ")
																+ currentFedState.name() + " @ its pseudo FED)");
											} else {
												existsAtLeaseOneFedBackpressured = true;
											}
										}
									}
									/* there are no FEDs in hierarchy */
									else {
										if (fed.getKey().getPercentBackpressure() == 0F) {
											result = true;
											contextHandler.register("FED", fed.getKey().getSrcIdExpected());
											contextHandler.register("FEDSTATE", currentFedState.name());
										} else {
											existsAtLeaseOneFedBackpressured = true;
										}

									}

									if (result) {
										contextHandler.register("TTCP", ttcp.getName());
										contextHandler.register("TTCPSTATE", currentState.name());
										contextHandler.register("SUBSYSTEM", subSystem.getName());
										
										if(currentState == TTSState.WARNING && "TRACKER".equalsIgnoreCase(subSystem.getName())){
											contextHandler.setActionKey("TRACKER-WARNING");
										} else{

											contextHandler.setActionKey(subSystem.getName());
										}
									}
								}
							}
							if (existsAtLeaseOneFedBackpressured) {
								// If there is at least one fed backpressured
								// this LM should not
								// fire (for more see github #83 issue)
								result = false;
							}
						}
					}
				}
			}
		}

		return result;
	}

}
