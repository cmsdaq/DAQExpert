package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

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

import java.util.Map;
import java.util.Set;

/**
 * Logic module identifying 3 flowchart case.
 *
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase3 extends KnownFailure {

	//TODO: include FED information
	public FlowchartCase3() {
		this.name = "Partition problem";
		this.description = "Partition {{PROBLEM-PARTITION}} in {{PROBLEM-SUBSYSTEM}} subsystem is in {{STATE}} TTS state. It's blocking triggers. The problem is caused by FED {{PROBLEM-FED}}";
		ConditionalAction action = new ConditionalAction("<<TTCHardReset>>",
				"If DAQ is still stuck after a few seconds: <<TTCHardReset>> (HardReset includes a Resync, so it may be used for both OOS and ERROR)",
				"Problem fixed: Make an e-log entry",
				"Problem not fixed: Try to recover: <<StopAndStartTheRun>> with <<RedAndGreenRecycle::{{PROBLEM-SUBSYSTEM}}>>. Try up to 2 times",
				"Problem still not fixed after recover: Call the DOC of {{PROBLEM-SUBSYSTEM}} (for the partition in {{STATE}})",
				"Problem fixed after recover: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (for the partition in {{STATE}}) to inform");

		action.addContextSteps("ECAL-LHC-UNSTABLE", "This problem is normal for ECAL in periods of unstable clock",
				"<<StopAndStartTheRun>> with <<RedAndGreenRecycle::ECAL>>",
				"Do not call the ECAL DOC");

		action.addContextSteps("ES-LHC-UNSTABLE", "This problem is normal for ES in periods of unstable clock",
				"<<StopAndStartTheRun>> with <<RedAndGreenRecycle::ES>>",
				"Do not call the ES DOC");

		this.action = action;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);

		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;
		assignPriority(results);
		
		boolean result = false;

		boolean isLhcClockAndUnstable = false;
		if("LHC".equalsIgnoreCase(daq.getClockSource()) && daq.getLhcClockStable() == false){
			isLhcClockAndUnstable = true;
		}


		String daqstate = daq.getDaqState();
		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {
					if (!ttcp.isMasked()) {

						TTSState currentState = getParitionState(ttcp);
						if (currentState == TTSState.OUT_OF_SYNC || currentState == TTSState.ERROR) {

							contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());
							contextHandler.register("PROBLEM-PARTITION", ttcp.getName());
							contextHandler.register("STATE", currentState.name());
							result = true;

							if(isLhcClockAndUnstable){
								contextHandler.setActionKey(subSystem.getName() + "-LHC-UNSTABLE");
							} else{
								contextHandler.setActionKey(subSystem.getName());
							}


                            Map<FED, Set<FED>> fedHierarchy = FEDHierarchyRetriever.getFEDHierarchy(ttcp);
                            for (Map.Entry<FED, Set<FED>> entry : fedHierarchy.entrySet()) {

                                if (entry.getValue().size() > 0) {
                                    for (FED fed : entry.getValue()) {

                                        if (TTSState.OUT_OF_SYNC.getCode().equalsIgnoreCase(fed.getTtsState())
                                                || TTSState.ERROR.getCode().equalsIgnoreCase(fed.getTtsState())) {
                                            contextHandler.register("PROBLEM-FED", fed.getSrcIdExpected());
                                        }
                                    }

                                } else {
                                    if (TTSState.OUT_OF_SYNC.getCode().equalsIgnoreCase(entry.getKey().getTtsState())
                                            || TTSState.ERROR.getCode().equalsIgnoreCase(entry.getKey().getTtsState())) {
                                        contextHandler.register("PROBLEM-FED", entry.getKey().getSrcIdExpected());
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
