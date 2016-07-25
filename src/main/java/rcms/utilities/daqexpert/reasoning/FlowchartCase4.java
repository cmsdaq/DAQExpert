package rcms.utilities.daqexpert.reasoning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;
import rcms.utilities.daqexpert.reasoning.base.TTSState;

/**
 * Logic module identifying 4 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase4 extends ExtendedCondition {

	public FlowchartCase4() {
		this.name = "CASE 4";
		this.description = "Exists TTCP in disconnected</br>"
				+ "TTS state of partition blocking trigger is Disconnected,"
				+ "The PI of the subsystem may be suffering from a firmware problem";
		this.action = Arrays.asList("Stop the run", "red & green recycle the subsystem corresponding to the partition",
				"Start new run", "Problem fixed: You are done make an e-log entry",
				"Problem not fixed: Call the DOC for the partition in disconnected");
		this.group = EventGroup.FL4;
		this.priority = EventPriority.critical;
	}

	private static final Logger logger = Logger.getLogger(FlowchartCase4.class);

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.DISCONNECTED) {

					logger.debug("M4: " + name + " at " + daq.getLastUpdate());
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
				if (currentState == TTSState.DISCONNECTED) {

					HashMap<String, Object> ttcpRaport = new HashMap<>();
					ttcpRaport.put("name", ttcp.getName());
					ttcpRaport.put("subsystem", ttcp.getSubsystem().getName());
					ttcpRaport.put("ttsState", TTSState.getByCode(ttcp.getTtsState()));
					eventRaport.getSetByCode("problemTTCP").add(ttcpRaport);

				}
			}
		}
	}

}
