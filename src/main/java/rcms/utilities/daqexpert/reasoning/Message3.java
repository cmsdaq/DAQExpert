package rcms.utilities.daqexpert.reasoning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.Aware;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.Level;
import rcms.utilities.daqexpert.reasoning.base.TTSState;

public class Message3 extends Aware implements Condition {

	private static Logger logger = Logger.getLogger(Message3.class);
	private final String ERROR_STATE = "ERROR";
	private static final String name = "TTCP in error or out of sync";
	private static final String description = "TTS state of partition blocking trigger is OutOfSync (OOS) or ERROR";
	private static final String action = "Issue a TTCHardReset, If DAQ is still stuck after a few seconds, issue another TTCHardReset (HardReset includes a Resync, so it may be used for both OOS and ERROR)";

	@Override
	public Boolean satisfied(DAQ daq) {

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
	public Level getLevel() {
		return Level.FL3; // change to flowchart
	}

	@Override
	public String getText() {
		return "M3: TTCP error or OOS";
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

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
