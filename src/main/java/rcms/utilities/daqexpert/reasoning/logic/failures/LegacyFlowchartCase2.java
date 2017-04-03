package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class LegacyFlowchartCase2 extends KnownFailure {

	public LegacyFlowchartCase2() {
		this.name = "Corrupted data received";
		this.description = "DAQ and level 0 in error state. "
				+ "A RU {{RU}} is in Failed state. A FED {{FED}} has sent corrupted data to the DAQ. "
				+ "Problem FED belongs to subsystem {{SUBSYSTEM}}";
		this.action = new SimpleAction(
				"Try to recover: Stop the run. Red & green recycle both the DAQ and the subsystem {{SUBSYSTEM}}. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{SUBSYSTEM}} (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{SUBSYSTEM}} (subsystem that sent corrupted data)");
	}

	private static Logger logger = Logger.getLogger(LegacyFlowchartCase2.class);
	private final String ERROR_STATE = "ERROR";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();
		boolean result = false;
		int i = 0;

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			if (ERROR_STATE.equalsIgnoreCase(l0state) && ERROR_STATE.equalsIgnoreCase(daqstate)) {

				for (FEDBuilder fb : daq.getFedBuilders()) {
					RU ru = fb.getRu();
					if (ru.getStateName().equalsIgnoreCase("Failed")) {

						i++;
						context.register("RU", ru.getHostname());
						result = true;
					}
				}

				for (FED fed : daq.getFeds()) {

					if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
						if (fed.getRuFedDataCorruption() > 0) {

							TTCPartition ttcp = fed.getTtcp();
							String ttcpName = "-";
							String subsystemName = "-";

							if (ttcp != null) {
								ttcpName = ttcp.getName();
								if (ttcp.getSubsystem() != null)
									subsystemName = ttcp.getSubsystem().getName();
							}
							context.register("FED", fed.getSrcIdExpected());
							context.register("TTCP", ttcpName);
							context.register("SUBSYSTEM", subsystemName);
							i++;
						}
					}
				}

				logger.debug("FC2 " + i);
			}
		}
		return result;
	}
}