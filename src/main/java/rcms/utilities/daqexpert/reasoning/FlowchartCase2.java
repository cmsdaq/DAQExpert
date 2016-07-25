package rcms.utilities.daqexpert.reasoning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
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
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase2 extends ExtendedCondition {

	public FlowchartCase2() {
		this.name = "CASE 2";
		this.description = "DAQ and level 0 in error state</br>"
				+ "A RU is in Failded state. A FED has sent corrupted data to the DAQ. "
				+ "Ru in failed state and subsystem attached below.";
		this.action = Arrays.asList(
				"Try to recover: Stop the run. Red & green recycle both the DAQ and the subsystem. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC for the subsystem that sent corrupted data to inform about the problem",
				"Problem not fixed: Call the DOC for the subsystem that sent corrupted data");
		this.group = EventGroup.FL2;
		this.priority = EventPriority.critical;
	}

	private static Logger logger = Logger.getLogger(FlowchartCase2.class);
	private final String ERROR_STATE = "ERROR";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
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

		for (FED fed : daq.getFeds()) {
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

}
