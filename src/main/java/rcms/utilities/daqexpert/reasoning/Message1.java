package rcms.utilities.daqexpert.reasoning;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.reasoning.base.Aware;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.Level;

public class Message1 extends Aware implements Condition {

	private static final Logger logger = Logger.getLogger(Message1.class);
	private static final String RUNBLOCKED_STATE = "RUNBLOCKED";
	private static final String message = "CASE 1";
	private static final String name = "cDAQ is stuck during STABLE BEAMS, no events flowing. DAQ and Level-0 are in RunBlocked state"; // events
	private static final String description = "A FED has sent out-of-sequence data to the DAQ. Corresponding subsystem and RU in SyncLoss state are attached below.";
	private static final String action = "Try to recover (try up to 2 times): If the subsystem is TRACKER: Stop the run, Start a new run. For any other subsystem: Stop the run. Red & green recycle the subsystem. Start a new Run. Problem not fixed: Call the DOC for the subsystem that caused the SyncLoss (attached below), Problem fixed: Make an e-log entry. Call the DOC for the subsystem that caused the SyncLoss (attached below) to informa about the problem";

	private RU problemRu;

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {

			for (FEDBuilder fb : daq.getFedBuilders()) {
				RU ru = fb.getRu();
				if (ru.getStatus().equalsIgnoreCase("SyncLoss")) {
					problemRu = ru;
				}

			}

			logger.debug(message);
			return true;
		}
		return false;
	}

	@Override
	public Level getLevel() {
		return Level.FL1;
	}

	@Override
	public String getText() {
		return message;
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

		EventRaport eventRaport = entry.getEventRaport();
		if (!eventRaport.isInitialized()) {
			eventRaport.initialize(name, description, action);
		}

		if (problemRu != null) {
			eventRaport.getSetByCode("problemRu").add(problemRu.getHostname() + ": " + problemRu.getStatus());
			for (FED fed : daq.getAllFeds()) {
				if (fed.getRuFedOutOfSync() > 0) {
					HashMap<String, String> fedraport = new HashMap<>();
					fedraport.put("sourceId", fed.getSrcIdExpected() + "");
					fedraport.put("noOfEventsOOS", fed.getRuFedOutOfSync() + "");
					fedraport.put("ttcp", fed.getTtcp() != null ? fed.getTtcp().getName() : "unavailable ttcp");
					fedraport.put("subsystem",
							fed.getTtcp() != null ? fed.getTtcp().getSubsystem() != null
									? fed.getTtcp().getSubsystem().getName() : "unavailable subsystem"
									: "unavailable ttcp");

					eventRaport.getSetByCode("fedsOutOfSync").add(fedraport);

				}

			}

		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
