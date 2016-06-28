package rcms.utilities.daqexpert.reasoning;

import java.util.ArrayList;
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
 * Logic module identifying 3 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase3 extends ExtendedCondition {

	public FlowchartCase3() {
		this.name = "CASE 3";
		this.description = "TTS state of partition blocking trigger is OutOfSync (OOS) or ERROR";
		this.action = "<ul><li>Issue a TTCHardReset</li>"
				+ "<li>If DAQ is still stuck after a few seconds, issue another TTCHardReset (HardReset includes a Resync, so it may be used for both OOS and ERROR)</li>"
				+ "<li>Problem fixed: Make an e-log entry</li>"
				+ "<li>Problem not fixed: Try to recover: Stop the run. Red & Green recycle the subsystem. Start a new run. Try up to 2 times.</li>"
				+ "<li>Problem still not fixed after recover: Call the DOC for the partition in error/OOS</li>"
				+ "<li>Problem fixed after recover: Make an e-log entry. Call the DOC for the partition in error/OOS to inform</li></ul>";

		this.group = EventGroup.FL3;
		this.priority = EventPriority.critical;
	}

	private static Logger logger = Logger.getLogger(FlowchartCase3.class);

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.OUT_OF_SYNC || currentState == TTSState.ERROR) {

					logger.debug("M3: " + name + " at " + daq.getLastUpdate());
					return true;
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
				if (currentState == TTSState.ERROR || currentState == TTSState.OUT_OF_SYNC) {
					HashMap<String, Object> ttcpRaport = new HashMap<>();
					ttcpRaport.put("name", ttcp.getName());
					ttcpRaport.put("subsystem", ttcp.getSubsystem().getName());
					ttcpRaport.put("ttsState", TTSState.getByCode(ttcp.getTtsState()));
					List<Object> oosFeds = new ArrayList<>();
					for (FED fed : ttcp.getFeds()) {
						if (fed.getRuFedOutOfSync() > 0) {
							HashMap<String, String> fedraport = new HashMap<>();
							fedraport.put("sourceId", fed.getSrcIdExpected() + "");
							fedraport.put("noOfEventsOOS", fed.getRuFedOutOfSync() + "");
							oosFeds.add(fedraport);
						}
					}
					eventRaport.getSetByCode("fedsInOOS").add(oosFeds);
					eventRaport.getSetByCode("problemTTCPs").add(ttcpRaport);
				}
			}
		}
	}

}
