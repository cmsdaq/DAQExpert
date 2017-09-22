package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying Before flowchart case 2.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class CorruptedData extends BackpressureAnalyzer {

	public CorruptedData() {
		this.name = "Corrupted data received";
		this.description = "Run blocked by corrupted data from FED {{PROBLEM-FED}} received by RU {{PROBLEM-RU}} which is now in failed state. "
				+ "Problem FED belongs to partition {{PROBLEM-TTCP}} in {{PROBLEM-SUBSYSTEM}} subsystem "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		/* default action */
		ConditionalAction action = new ConditionalAction(
				"Try to recover: Stop the run. Red & green recycle both the DAQ and the subsystem {{PROBLEM-SUBSYSTEM}}. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data)");
		
		/* ecal specific case */
		action.addContextSteps("ECAL", "Try to stop/start the run (Red recycle DAQ only)",
				"If this doesn't help: Stop the run. Red & green recycle both the DAQ and the subsystem {{PROBLEM-SUBSYSTEM}}. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data)");

		this.action = action;

		
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.CorruptedDataReceived) {
			result = true;
		}
		return result;
	}
}