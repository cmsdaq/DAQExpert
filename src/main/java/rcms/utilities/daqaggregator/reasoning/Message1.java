package rcms.utilities.daqaggregator.reasoning;

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

public class Message1 implements Condition {

	private static Logger logger = Logger.getLogger(Message1.class);
	private final String RUNBLOCKED_STATE = "RUNBLOCKED";

	private String message;

	private RU problemRu;
	private String name = "cDAQ is stack during STABLE BEAMS, no events flowing. DAQ and Level-0 are in RunBlocked state";
	private String description = "A FED has sent out-of-sequence data to the DAQ. Corresponding subsystem and RU in SyncLoss state are attached below.";
	private String action = "Try to recover (try up to 2 times): If the subsystem is TRACKER: Stop the run, Start a new run. For any other subsystem: Stop the run. Red & green recycle the subsystem. Start a new Run. Problem not fixed: Call the DOC for the subsystem that caused the SyncLoss (attached below), Problem fixed: Make an e-log entry. Call the DOC for the subsystem that caused the SyncLoss (attached below) to informa about the problem";

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!"Stable Beams".equalsIgnoreCase(daq.getLhcBeamMode()))
			return false;

		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {

			for (FEDBuilder fb : daq.getFedBuilders()) {
				RU ru = fb.getRu();
				if (ru.getStatus().equalsIgnoreCase("SyncLoss")) {
					problemRu = ru;
				}

			}

			message = "M1: DAQ and L0 in RUNBLOCKED";
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
					String fedString = "FED id: " + fed.getId() + ", FED expected id: " + fed.getSrcIdExpected()
							+ ", FED OOS: " + fed.getRuFedOutOfSync() + ", FED ttcp: " + fed.getTtcp();
					eventRaport.getSetByCode("fedsOutOfSync").add(fedString);
				}

			}

		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
