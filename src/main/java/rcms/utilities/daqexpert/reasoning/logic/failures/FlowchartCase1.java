package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 * Logic module identifying 1 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlowchartCase1 extends KnownFailure {

	/** regex for getting ttc partition and FED source id which caused the sync loss from the RU exception message */
	private final Pattern syncLossPattern = Pattern.compile("Caught exception: exception::MismatchDetected 'Mismatch detected: expected evb id .*, but found evb id .* in data block from FED (\\d+) \\((.+)\\)' raised at");

	public FlowchartCase1() {
		this.name = "FC1";

		this.description = "Run blocked by out-of-sync data from FED {{FED}}, RU {{RU}} is in syncloss. "
				+ "Problem FED belongs to TTCP {{TTCP}} in {{SUBSYSTEM}} subsystem";

		/* Default action */
		ConditionalAction action = new ConditionalAction("Try to recover (try up to 2 times)",
				"Stop the run. Red & green recycle the subsystem. Start a new Run",
				"Problem not fixed: Call the DOC of {{SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		/* SUBSYSTEM=Tracker action */
		action.addContextSteps("TRACKER", "Try to recover (try up to 2 times)", "Stop the run, Start a new run.",
				"Problem not fixed: Call the DOC of {{SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		this.action = action;
	}

	private static final String RUNBLOCKED_STATE = "RUNBLOCKED";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? EventPriority.CRITICAL : EventPriority.DEFAULTT;

		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {
			for (RU ru : daq.getRus()) {
				if ("SyncLoss".equalsIgnoreCase(ru.getStateName())) {
					context.register("RU", ru.getHostname());
				}
			}

			for (FED fed : daq.getFeds()) {
				if (fed.getRuFedOutOfSync() > 0) {

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
					context.setActionKey(subsystemName);

				}

			}
			return true;
		}

		return false;
	}

}
