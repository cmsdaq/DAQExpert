package rcms.utilities.daqexpert.reasoning;

import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;
import rcms.utilities.daqexpert.reasoning.base.TTSState;

/**
 * Logic module identifying 5 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase5 extends ExtendedCondition {

	public FlowchartCase5() {
		this.name = "FC5";
		this.description = "FC5: TTCP {{TTCP}} of {{SUBSYSTEM}} subsystem is blocking trigger, it's in {{TTCPSTATE}} TTS state, "
				+ "The problem is caused by FED {{FED}} in {{FEDSTATE}}";
		this.action = Arrays.asList("Stop the run",
				"If the problem is caused by an ECAL FED in Busy proceed to 3rd step. Otherwise red & green recycle the subsystem.",
				"Start new run (try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem in Warning/Busy to inform",
				"Call the DOC for the subsystem in Warning/Busy");
		this.group = EventGroup.FL5;
		this.priority = EventPriority.critical;
	}

	private static final Logger logger = Logger.getLogger(FlowchartCase5.class);
	private static final String RUNBLOCKED_STATE = "RUNBLOCKED";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean result = false;

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;
		
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();
		
		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate))
			return false;

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
							result = true;
						}
					}
				}
			}
		}

		return result;
	}
}
