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
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.CorruptedData;
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
		this.description = "TTCP {{PROBLEM-PARTITION}} of {{PROBLEM-SUBSYSTEM}} subsystem is blocking triggers, it's in {{TTCPSTATE}} TTS state, "
				+ "The problem is caused by FED {{PROBLEM-FED}} in {{FEDSTATE}}";
		this.briefDescription = "{{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}} is stuck in TTS state {{TTCPSTATE}}";

		/* default action */
		ConditionalAction action = new ConditionalAction(
				"<<StopAndStartTheRun>> with <<RedRecycle::{{PROBLEM-SUBSYSTEM}}>> and <<GreenRecycle::{{PROBLEM-SUBSYSTEM}}>> (try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{PROBLEM-SUBSYSTEM}} to inform",
				"Problem not fixed: Call the DOC for the subsystem {{PROBLEM-SUBSYSTEM}}");

		/* ecal specific case */
		action.addContextSteps("ECAL", "<<StopAndStartTheRun>> (try up to 2 times)",
				"Problem fixed: Make an e-log entry.", "Problem not fixed: Red recycle ECAL",
				"Call the DOC for the ECAL");

		//TODO: update multistep recovery: when fixed update integration test JobManagerIt.blackboxTest1
		/* tracker specific case only when warning state */
		action.addContextSteps("TRACKER", "Issue a TTCResync once", "Problem fixed: Make an e-log entry.",
				"Problem not fixed: Stop the run, red recycle TRACKER, start a new run",
				"Problem still not fixed: Call the DOC for the TRACKER");

		/* gem 1467 specific case */
		action.addContextSteps("GEM-1467-BUSY", "<<StopAndStartTheRun>> with <<GreenRecycle::GEM>> (try up to 3 times)",
				"Whether the above helped or not, call the GEM DOC and write an ELOG about the actions taken and the results obtained");

		this.action = action;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);

		declareAffected(LogicModuleRegistry.NoRateWhenExpected);

		declareCause(LogicModuleRegistry.CorruptedData);
		//? declareCause(LogicModuleRegistry.BugInFilterfarm); //TODO: can this be caused by
		declareCause(LogicModuleRegistry.HLTProblem);
		declareCause(LogicModuleRegistry.RuStuckWaiting);
		declareCause(LogicModuleRegistry.RuStuck);
		declareCause(LogicModuleRegistry.LinkProblem);
		declareCause(LogicModuleRegistry.FlowchartCase1);
		declareCause(LogicModuleRegistry.RuStuckWaitingOther);
		declareCause(LogicModuleRegistry.OutOfSequenceData);
	}

	// add triggers info (behind or the same
	// number)
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
												contextHandler.register("PROBLEM-FED",
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
											contextHandler.register("PROBLEM-FED", fed.getKey().getSrcIdExpected());
											contextHandler.register("FEDSTATE", currentFedState.name());

										} else {
											existsAtLeaseOneFedBackpressured = true;
										}

									}

									if (result) {
										contextHandler.register("PROBLEM-PARTITION", ttcp.getName());
										contextHandler.register("TTCPSTATE", currentState.name());
										contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());

										if("GEM".equalsIgnoreCase(contextHandler.getContextEntry("PROBLEM-SUBSYSTEM").getTextRepresentation())
												&& "1467".equalsIgnoreCase(contextHandler.getContextEntry("PROBLEM-FED").getTextRepresentation())
												&& "BUSY".equalsIgnoreCase(contextHandler.getContextEntry("FEDSTATE").getTextRepresentation())){
											contextHandler.setActionKey("GEM-1467-BUSY");
										} else {
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
