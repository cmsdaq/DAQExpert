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

public class Message2 extends Aware implements Condition {

	private static Logger logger = Logger.getLogger(Message2.class);
	private final String ERROR_STATE = "ERROR";
	private static final String name = "DAQ and level 0 in error state";
	private static final String description = "cDAQ is stack during STABLE BEAMS, no events flowing. DAQ and Level-0 are in Error state, exists RU in Failed state. A FED has sent corrupted data to the DAQ. Corresponding system of the FED and RU in Failde state attached below.";
	private static final String action = "Try to recover: Stop the run. Red & green recycle both the DAQ and the subsystem. Start new Run. (try up to 2 times). Problem not fixed: Call the DOC for the subsystem that sent currupted data, Problem fixed: Make an e-log entry. Call the DOC for the subsystem that sent corrupted data to informa about the problem.";

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		if (ERROR_STATE.equalsIgnoreCase(l0state) && ERROR_STATE.equalsIgnoreCase(daqstate)) {

			for (FEDBuilder fb : daq.getFedBuilders()) {
				RU ru = fb.getRu();
				if (ru.getStatus().equalsIgnoreCase("Failed")) {

					logger.debug("M2 DAQ and level 0 in error state, exists RU in failed state");
					return true;
				}
			}

			return false;
		}
		return false;
	}

	@Override
	public Level getLevel() {
		return Level.FL2;
	}

	@Override
	public String getText() {
		return "CASE 2";
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

		EventRaport eventRaport = entry.getEventRaport();
		if (!eventRaport.isInitialized()) {
			eventRaport.initialize(name, description, action);
		}

		for (FEDBuilder fb : daq.getFedBuilders()) {
			RU ru = fb.getRu();
			if (ru.getStatus().equalsIgnoreCase("Failed")) {
				eventRaport.getSetByCode("problemRu").add(ru.getHostname() + ": " + ru.getStatus());
			}
		}

		for (FED fed : daq.getAllFeds()) {
			if (fed.getRuFedDataCorruption() > 0) {

				HashMap<String, String> fedraport = new HashMap<>();
				fedraport.put("expectedId", fed.getSrcIdExpected() + "");
				fedraport.put("corrupted", fed.getRuFedDataCorruption() + "");
				fedraport.put("ttcp", fed.getTtcp() != null ? fed.getTtcp().getName() : "unavailable ttcp");
				fedraport.put("subsystem",
						fed.getTtcp() != null ? fed.getTtcp().getSubsystem() != null
								? fed.getTtcp().getSubsystem().getName() : "unavailable subsystem"
								: "unavailable ttcp");

				eventRaport.getSetByCode("corruptedFeds").add(fedraport);

			}
		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
