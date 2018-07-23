package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
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
				+ "Problem FED belongs to partition {{PROBLEM-PARTITION}} in {{PROBLEM-SUBSYSTEM}} subsystem "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		this.briefDescription = "Run blocked by corrupted data from FED(s) {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}}";

		/* default action */
		ConditionalAction action = new ConditionalAction(
				"Try to recover: <<StopAndStartTheRun>> with both <<RedAndGreenRecycle::{{PROBLEM-SUBSYSTEM}}>> and <<RedAndGreenRecycle::DAQ>> (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data)");
		
		/* ecal specific case */
		action.addContextSteps("ECAL", "<<StopAndStartTheRun>> with <<RedAndGreenRecycle::DAQ>>",
				"If this doesn't help: <<StopAndStartTheRun>> with both <<RedAndGreenRecycle::ECAL>> and <<RedAndGreenRecycle::DAQ>>",
				"Problem fixed: Make an e-log entry. If this happen during physics data taking call the DOC of ECAL (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of ECAL (subsystem that sent corrupted data)\n");

		this.action = action;

		
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);

		declareAffected(LogicModuleRegistry.TTSDeadtime);
		declareAffected(LogicModuleRegistry.RuFailed);
		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
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