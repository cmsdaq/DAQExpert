package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.EventRaport;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.TTSState;

public class Message3 implements Condition {

	private static Logger logger = Logger.getLogger(Message3.class);
	private final String ERROR_STATE = "ERROR";
	private static final String name = "TTCP in error or out of sync";
	private static final String description = "TTS state of partition blocking trigger is OutOfSync (OOS) or ERROR";
	private static final String action = "Issue a TTCHardReset, If DAQ is still stuck after a few seconds, issue another TTCHardReset (HardReset includes a Resync, so it may be used for both OOS and ERROR)";

	@Override
	public Boolean satisfied(DAQ daq) {
		if (!"Stable Beams".equalsIgnoreCase(daq.getLhcBeamMode()))
			return false;
		// TODO: this code is duplicated, rate zero is already checked in other
		// logic modules
		float rate = daq.getFedBuilderSummary().getRate();
		if (rate != 0)
			return false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				TTSState currentState = TTSState.getByCode(ttcp.getTtsState());
				if (currentState == TTSState.OUT_OF_SYNC || currentState == TTSState.ERROR) {

					logger.info(
							"M3 DAQ and level 0 in error state, exists TTCP in state non ready " + daq.getLastUpdate());
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
				if (currentState == TTSState.ERROR) {
					eventRaport.getSetByCode("ttcpInError")
							.add(subSystem.getName() + ": " + ttcp.getName() + ": " + ttcp.getTtsState());
				} else if (currentState == TTSState.OUT_OF_SYNC) {
					eventRaport.getSetByCode("ttcpInOOS")
							.add(subSystem.getName() + ": " + ttcp.getName() + ": " + ttcp.getTtsState());
				}
			}
		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}
