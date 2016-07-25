package rcms.utilities.daqexpert.reasoning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
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
		this.name = "CASE 5";
		this.description = "TTCP blocking trigger in busy or warning TTS state, FED not backpressured by cDAQ</br>"
				+ "The problem is caused by FED in Busy/Warning (see FED attached below)</br>"
				+ "Note that Expert has access only to legacy FEDs, follow Flowchart if no FED attached below";
		this.action = Arrays.asList("Stop the run",
				"If the problem is caused by an ECAL FED in Busy proceed to 3rd step. Otherwise red & green recycle the subsystem.",
				"Start new run (try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem in Warning/Busy to inform",
				"Call the DOC for the subsystem in Warning/Busy");
		this.group = EventGroup.FL5;
		this.priority = EventPriority.critical;
	}

	private static final Logger logger = Logger.getLogger(FlowchartCase5.class);

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

					for (FED fed : ttcp.getFeds()) {
						TTSState currentFedState = TTSState.getByCode(fed.getTtsState());
						if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)
								&& fed.getPercentBackpressure() == 0F) {

							logger.debug("M5: " + name + " with fed " + fed.getId() + " in backpressure at "
									+ new Date(daq.getLastUpdate()));
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

		EventRaport eventRaport = entry.getEventRaport();
		if (!eventRaport.isInitialized()) {
			eventRaport.initialize(name, description, action);
		}

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

					HashMap<String, Object> ttcpRaport = new HashMap<>();
					ttcpRaport.put("name", ttcp.getName());
					ttcpRaport.put("subsystem", ttcp.getSubsystem().getName());
					ttcpRaport.put("ttsState", TTSState.getByCode(ttcp.getTtsState()));
					List<Object> problemFeds = new ArrayList<>();
					for (FED fed : ttcp.getFeds()) {

						TTSState currentFedState = TTSState.getByCode(fed.getTtsState());
						if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)
								&& fed.getPercentBackpressure() == 0F) {
							HashMap<String, String> fedraport = new HashMap<>();
							fedraport.put("sourceId", fed.getSrcIdExpected() + "");
							fedraport.put("ttsState", TTSState.getByCode(fed.getTtsState()).name());
							problemFeds.add(fedraport);
						}
					}
					eventRaport.getSetByCode("problemTTCPs").add(ttcpRaport);
					eventRaport.getSetByCode("problemFEDs").add(problemFeds);

				}
			}
		}
	}

}
