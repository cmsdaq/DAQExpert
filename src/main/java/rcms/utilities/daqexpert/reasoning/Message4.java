package rcms.utilities.daqexpert.reasoning;

import java.util.HashMap;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.Aware;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.Level;
import rcms.utilities.daqexpert.reasoning.base.TTSState;

public class Message4 extends Aware implements Condition {

	private static Logger logger = Logger.getLogger(Message4.class);
	private final String ERROR_STATE = "ERROR";
	private static final String name = "Exists TTCP in disconnected";
	private static final String description = "TTS state of partition blocking trigger is Disconnected, The PI of the subsystem may be suffering from a firmware problem";
	private static final String action = "Stop the run, red & green recycle the subsystem corresponding to the partition, Start new run";

	@Override
	public Boolean satisfied(DAQ daq) {
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
	public Level getLevel() {
		return Level.FL4; // change to flowchart
	}

	@Override
	public String getText() {
		return "M4: Disconnected TTCP ";
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

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
