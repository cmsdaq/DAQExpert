package rcms.utilities.daqexpert.reasoning;

import java.util.ArrayList;
import java.util.Date;
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

public class Message5 extends Aware implements Condition {

	private static Logger logger = Logger.getLogger(Message5.class);
	private final String ERROR_STATE = "ERROR";
	private static final String name = "TTCP in busy or warning, and fed not backpressured by cDAQ";
	private static final String description = "The problem is caused by FED in Busy/Warning";
	private static final String action = "";

	@Override
	public Boolean satisfied(DAQ daq) {

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
	public Level getLevel() {
		return Level.FL5; // change to flowchart
	}

	@Override
	public String getText() {
		return "M5" + name;
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

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
