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

	/** sets keys FED, TTCP and SUBSYSTEM to the given string */ 
	private void setContextValues(String text) {
		
		context.register("FED", text);
		context.register("TTCP", text);
		context.register("SUBSYSTEM", text);
		
	}
	
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		String daqstate = daq.getDaqState();

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? EventPriority.CRITICAL : EventPriority.DEFAULTT;
		
		// note that the l0state may e.g. be 'Error' 
		if (RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {

			// for the moment, just find the first RU in SyncLoss
			RU syncLossRU = null;

			for (RU ru : daq.getRus()) {
				if ("SyncLoss".equalsIgnoreCase(ru.getStateName())) {
					context.register("RU", ru.getHostname());
					syncLossRU = ru;
					break;
				}
			}

			// subsystem not yet known
			context.setActionKey("(unknown subsystem)");

			if (syncLossRU == null) {
				// no RU in syncloss found, we don't know FED, TTCP and SUBSYSTEM
				setContextValues("(RU not found)");
				
			} else {
 	    
				// find the FED from the exception message
				//
				// example message:
				// Caught exception: exception::MismatchDetected 'Mismatch detected: expected evb id runNumber=286488 lumiSection=301 resyncCount=4 eventNumber=1247256 bxId=1459, but found evb id runNumber=286488 *resyncCount=5 eventNumber=1 bxId=2206 in data block from FED 548 (ES)' raised at append(/usr/local/src/xdaq/baseline13/trunk/daq/evb/src/common/readoutunit/SuperFragment.cc:32)

				Matcher mo = syncLossPattern.matcher(syncLossRU.getErrorMsg());
				if (mo.find())
				{
					int fedId = Integer.parseInt(mo.group(1));
					context.register("FED", mo.group(1));

					// get the FED object
					FED problematicFED = daq.getFEDbySrcId(fedId);
					
					if (problematicFED != null)
					{
						TTCPartition ttcp = problematicFED.getTtcp();
						String ttcpName = "-";
						String subsystemName = "-";

						if (ttcp != null)
						{
							ttcpName = ttcp.getName();
							if (ttcp.getSubsystem() != null) {
								subsystemName = ttcp.getSubsystem().getName();
							}
						}
						context.register("TTCP", ttcpName);
						context.register("SUBSYSTEM", subsystemName);
						context.setActionKey(subsystemName);
					} else {
						setContextValues("(FED not found)");
					}

				} else {
					// regex did not match, probably the format of the exception message
					// in the event build has changed, need to change the regex
					// pattern above
					setContextValues("(regex mismatch)");
				}

			} // RU in syncloss state found

			return true;
		} // if runblocked state

		return false;
	}

}
