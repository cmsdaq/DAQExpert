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
 * Logic module identifying 6 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase6 extends ExtendedCondition {

	public FlowchartCase6() {
		this.name = "CASE 6";
		this.description = "TTCP in busy or warning, and FED backpressured</br>"
				+ "Backpressure is going through the FED in Busy/Warning but there is NOTHING wrong with the FED in Busy/Warning.</br>"
				+ "A FED stopped sending data (attached below)"
				+ "Note that Expert has access only to legacy FEDs, follow Flowchart if no FED attached below";
		this.action = Arrays.asList("Try to recover: Stop the run",
				"Red & green recycle the subsystem whose FED stopped sending data", "Start new Run (Try 1 time)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem whose FED stopped sending data to inform",
				"Problem not fixed: Call the OC for the subsystem whose FED stopped sending data");
		this.group = EventGroup.FL6;
		this.priority = EventPriority.critical;
	}

	private static final Logger logger = Logger.getLogger(FlowchartCase6.class);

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
								&& fed.getPercentBackpressure() > 0F) {

							logger.debug("M6: " + name + " with fed " + fed.getId() + " in backpressure at "
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
						if (fed.getPercentBackpressure() > 0F) {

							HashMap<String, Object> fedraport = new HashMap<>();
							fedraport.put("sourceId", fed.getSrcIdExpected() + "");
							fedraport.put("backpressured", true); // FIXME put
																	// data in %
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
