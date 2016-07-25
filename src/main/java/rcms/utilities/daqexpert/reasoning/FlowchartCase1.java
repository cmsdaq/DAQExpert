package rcms.utilities.daqexpert.reasoning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;

/**
 * Logic module identifying 1 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase1 extends ExtendedCondition {

	public FlowchartCase1() {
		this.name = "CASE 1";
		this.description = "Run blocked by OOS FED data and RU in SYNCLOSS</br>"
				+ "DAQ and Level-0 are in RunBlocked state. A FED has sent out-of-sequence data to the DAQ. "
				+ "Corresponding subsystem and RU in SyncLoss state are attached below.";
		this.action = Arrays.asList("Try to recover (try up to 2 times)",
				"If the subsystem is TRACKER: Stop the run, Start a new run. ",
				"For any other subsystem: Stop the run. Red & green recycle the subsystem. Start a new Run",
				"Problem not fixed: Call the DOC for the subsystem that caused the SyncLoss (attached below)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC for the subsystem that caused the SyncLoss (attached below) to inform about the problem");
		this.group = EventGroup.FL1;
		this.priority = EventPriority.critical;
	}

	private static final Logger logger = Logger.getLogger(FlowchartCase1.class);
	private static final String RUNBLOCKED_STATE = "RUNBLOCKED";
	private RU problemRu;

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {

			for (RU ru : daq.getRus()) {
				if ("SyncLoss".equalsIgnoreCase(ru.getStatus())) {
					problemRu = ru;
				}
			}

			logger.debug(name);
			return true;
		}
		return false;
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

		EventRaport eventRaport = entry.getEventRaport();
		if (!eventRaport.isInitialized()) {
			eventRaport.initialize(name, description, action);
		}

		if (problemRu != null) {
			eventRaport.getSetByCode("problemRu").add(problemRu.getHostname() + ": " + problemRu.getStatus());
			for (FED fed : daq.getFeds()) {
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

}
