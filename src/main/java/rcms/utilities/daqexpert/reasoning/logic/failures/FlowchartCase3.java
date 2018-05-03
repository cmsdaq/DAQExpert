package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.Action;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying 3 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase3 extends KnownFailure {

	public FlowchartCase3() {
		this.name = "Partition problem";
		this.description = "Partition {{TTCP}} in {{SUBSYSTEM}} subsystem is in {{STATE}} TTS state. It's blocking triggers.";
		ConditionalAction action = new ConditionalAction("Issue a TTCHardReset",
				"If DAQ is still stuck after a few seconds, issue another TTCHardReset (HardReset includes a Resync, so it may be used for both OOS and ERROR)",
				"Problem fixed: Make an e-log entry",
				"Problem not fixed: Try to recover: Stop the run. Red & Green recycle the subsystem {{SUBSYSTEM}}. Start a new run. Try up to 2 times",
				"Problem still not fixed after recover: Call the DOC of {{SUBSYSTEM}} (for the partition in {{STATE}})",
				"Problem fixed after recover: Make an e-log entry. Call the DOC of {{SUBSYSTEM}} (for the partition in {{STATE}}) to inform");

		action.addContextSteps("ECAL-LHC-UNSTABLE", "This problem is normal for ECAL in periods of unstable clock",
				"Stop the run, Red recycle ECAL and start a new run",
				"Do not call the ECAL DOC");

		action.addContextSteps("ES-LHC-UNSTABLE", "This problem is normal for ES in periods of unstable clock",
				"Stop the run, Red recycle ES and start a new run",
				"Do not call the ES DOC");

		this.action = action;

	}

	@Override
	public void declareRequired(){
		require(LogicModuleRegistry.NoRateWhenExpected);
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

							contextHandler.register("SUBSYSTEM", subSystem.getName());
							contextHandler.register("TTCP", ttcp.getName());
							contextHandler.register("STATE", currentState.name());
							result = true;

							if(isLhcClockAndUnstable){
								contextHandler.setActionKey(subSystem.getName() + "-LHC-UNSTABLE");
							} else{
								contextHandler.setActionKey(subSystem.getName());
							}
						}
					}
				}
			}
		}

		return result;
	}

}
