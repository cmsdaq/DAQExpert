package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OutOfSequenceData;

/**
 * Logic module identifying 1 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * @author holzner
 *
 */
public class LegacyFlowchartCase1 extends KnownFailure {

	/**
	 * regex for getting ttc partition and FED source id which caused the sync
	 * loss from the RU exception message
	 */
	private final Pattern syncLossPattern = Pattern.compile(" FED (\\d+) \\((.+)\\)");

	public LegacyFlowchartCase1() {
		this.name = "Out of sequence data received";

		this.description = "Run blocked by out-of-sync data from FED {{PROBLEM-FED}}, RU {{RU}} is in syncloss. "
				+ "Problem FED belongs to TTCP {{PROBLEM-TTCP}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "Original error message: {{ORIGERRMSG}}";

		/* Default action */
		ConditionalAction action = new ConditionalAction("Try to recover (try up to 2 times)",
				"<<StopAndStartTheRun>> with <<RedRecycle::{{PROBLEM-SUBSYSTEM}}>> & <<GreenRecycle::{{PROBLEM-SUBSYSTEM}}>>",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		/* SUBSYSTEM=Tracker action */
		action.addContextSteps("TRACKER", "Try to recover (try up to 2 times)",
				"<<StopAndStartTheRun>>",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		//TODO: adapt to multistep recovery
		/* ecal specific case */
		action.addContextSteps("ECAL", "Try to stop/start the run",
				"If this doesn't help: Stop the run. Red & green recycle both the DAQ and the subsystem {{PROBLEM-SUBSYSTEM}}. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent out-of-sync data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent out-of-sync data data)");

		/* FED=1111 */
		action.addContextSteps("FED1111or1109", "<<StopAndStartTheRun>>",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		this.action = action;
	}

	@Override
	public void declareRequired(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		require(LogicModuleRegistry.OutOfSequenceData);
	}

	private static final String RUNBLOCKED_STATE = "RUNBLOCKED";

	/** sets keys FED, TTCP and SUBSYSTEM to the given string */
	private void setContextValues(String text) {

		contextHandler.register("PROBLEM-FED", text);
		contextHandler.register("PROBLEM-TTCP", text);
		contextHandler.register("PROBLEM-SUBSYSTEM", text);
		contextHandler.register("ORIGERRMSG", "-");

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;

		if (results.get(OutOfSequenceData.class.getSimpleName()).getResult())
			return false;

		assignPriority(results);

		String daqstate = daq.getDaqState();
		// note that the l0state may e.g. be 'Error'

		// we do not require anymore that DAQ is in 'RunBlocked' state for the
		// moment
		// since sometimes the state is not propagated properly
		//
		// SyncLoss state of the RU is a strong enough signal that we have
		// the problem this class should detect
		{

			// for the moment, just find the first RU in SyncLoss
			RU syncLossRU = null;

			for (RU ru : daq.getRus()) {
				if ("SyncLoss".equalsIgnoreCase(ru.getStateName())) {
					contextHandler.register("RU", ru.getHostname());
					syncLossRU = ru;
					break;
				}
			}

			if (syncLossRU == null) {

				// we insist that there is at least one RU in SyncLoss state
				return false;

			} else {

				String originalMessage = syncLossRU.getErrorMsg();
				String trimmedMessage = originalMessage;
				int openingQuote = originalMessage.indexOf("'", 0);
				if (-1 != openingQuote) {
					int closingQuote = originalMessage.indexOf("'", openingQuote + 1);
					if (-1 != closingQuote) {
						// both opening and closing quote found,
						int additionalQuote = originalMessage.indexOf("'", closingQuote + 1);
						if (-1 == additionalQuote) {
							// there is no additional quotes - for sure this is
							// the only quotes, we can trim
							trimmedMessage = originalMessage.substring(openingQuote, closingQuote);
						} else {
							System.out.println("Cannot trim the message: " + originalMessage + " too many quotes: "
									+ openingQuote + ", " + closingQuote + ", " + additionalQuote);
						}
					}
				}

				contextHandler.register("ORIGERRMSG", trimmedMessage);

				// find the FED from the exception message
				//
				// example message:
				// Caught exception: exception::MismatchDetected 'Mismatch
				// detected: expected evb id runNumber=286488 lumiSection=301
				// resyncCount=4 eventNumber=1247256 bxId=1459, but found evb id
				// runNumber=286488 *resyncCount=5 eventNumber=1 bxId=2206 in
				// data block from FED 548 (ES)' raised at
				// append(/usr/local/src/xdaq/baseline13/trunk/daq/evb/src/common/readoutunit/SuperFragment.cc:32)

				Matcher mo = syncLossPattern.matcher(syncLossRU.getErrorMsg());
				if (mo.find()) {
					int fedId = Integer.parseInt(mo.group(1));
					contextHandler.register("PROBLEM-FED", mo.group(1));

					// get the FED object
					FED problematicFED = daq.getFEDbySrcId(fedId);

					if (problematicFED != null) {
						TTCPartition ttcp = problematicFED.getTtcp();
						String ttcpName = "-";
						String subsystemName = "-";

						if (ttcp != null) {
							ttcpName = ttcp.getName();
							if (ttcp.getSubsystem() != null) {
								subsystemName = ttcp.getSubsystem().getName();
							}
						}
						contextHandler.register("PROBLEM-TTCP", ttcpName);
						contextHandler.register("PROBLEM-SUBSYSTEM", subsystemName);

						if (problematicFED.getSrcIdExpected() == 1111 || problematicFED.getSrcIdExpected() == 1109) {
							contextHandler.setActionKey("FED1111or1109");
						} else {
							contextHandler.setActionKey(subsystemName);
						}
					} else {
						setContextValues("(FED not found)");
					}

				} else {
					// regex did not match, probably the format of the exception
					// message
					// in the event build has changed, need to change the regex
					// pattern above
					setContextValues("(regex mismatch)");
				}

			} // RU in syncloss state found

			return true;
		}
	}

}
