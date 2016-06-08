package rcms.utilities.daqaggregator.reasoning;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.EventRaport;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class Message2 implements Condition {

	private static Logger logger = Logger.getLogger(Message2.class);
	private final String ERROR_STATE = "ERROR";
	private static final String name = "cDAQ is stack during STABLE BEAMS, no events flowing. DAQ and Level-0 are in Error state, exists RU in Failed state";
	private static final String description = "A FED has sent corrupted data to the DAQ. Corresponding system of the FED and RU in Failde state attached below.";
	private static final String action = "Try to recover: Stop the run. Red & green recycle both the DAQ and the subsystem. Start new Run. (try up to 2 times). Problem not fixed: Call the DOC for the subsystem that sent currupted data, Problem fixed: Make an e-log entry. Call the DOC for the subsystem that sent corrupted data to informa about the problem.";

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!"Stable Beams".equalsIgnoreCase(daq.getLhcBeamMode()))
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
		return "M2: DAQ and level 0 in error state";
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
				String fedString = "FED id: " + fed.getId() + ", FED expected id: " + fed.getSrcIdExpected()
						+ ", FED corrupted: " + fed.getRuFedDataCorruption();

				eventRaport.getSetByCode("corruptedFeds").add(fedString);
			}
			if (fed.isRuFedInError()) {
				String fedString = "FED id: " + fed.getId() + ", FED expected id: " + fed.getSrcIdExpected();
				eventRaport.getSetByCode("fedsInError").add(fedString);
			}
		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
