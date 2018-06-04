package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying out of sequence data received (1st flowchart case)
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class OutOfSequenceData extends BackpressureAnalyzer {

	public OutOfSequenceData() {
		this.name = "Out of sequence data received";

		this.description = "Run blocked by out-of-sync data from FED {{PROBLEM-FED}} received by RU {{PROBLEM-RU}} - now in syncloss state. "
				+ "Problem FED belongs to partition {{PROBLEM-TTCP}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		this.briefDescription = "Run blocked by out-of-sync data from FED(s) {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-TTCP}}/{{PROBLEM-FED}}";

		/* Default action */
		ConditionalAction action = new ConditionalAction(
				"<<StopAndStartTheRun>> with <<RedRecycle::{{PROBLEM-SUBSYSTEM}}>> & <<GreenRecycle::{{PROBLEM-SUBSYSTEM}}>> using L0 Automator",
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
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		declareAffected(LogicModuleRegistry.FlowchartCase5);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;

		assignPriority(results);

		boolean result = false;

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.OutOfSequenceDataReceived) {
			result = true;
		}

		return result;
	}

}
