package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.HavingSpecialInstructions;

/**
 * Logic module identifying Before flowchart case 2.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class CorruptedData extends BackpressureAnalyzer implements HavingSpecialInstructions {

	public CorruptedData() {
		this.name = "Corrupted data received";
		this.description = "Run blocked by corrupted data from FED {{PROBLEM-FED}} received by RU {{PROBLEM-RU}} which is now in failed state. "
				+ "Problem FED belongs to partition {{PROBLEM-PARTITION}} in {{PROBLEM-SUBSYSTEM}} subsystem "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-PARTITION}} of {{AFFECTED-SUBSYSTEM}}";

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

		/* GEM in collisions */
		action.addContextSteps("GEM-collisions", "Stop the run",
							   "Select the keepAlive option for GEM in the FED panel",
							   "Put GEM in local", "Start a new run without GEM",
							   "Call the GEM DOC. - This way the GEM DOC will take debug information");


		this.action = action;

		
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		require(LogicModuleRegistry.StableBeams);

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

	@Override
	public String selectSpecialInstructionKey(DAQ daq, Map<String, Output> results) {

		String problemSubsystem = contextHandler.getContextEntry("PROBLEM-SUBSYSTEM").getTextRepresentation();
		boolean stableBeams = results.get(StableBeams.class.getSimpleName()).getResult();

		if(problemSubsystem!=null) {
			switch (problemSubsystem) {
				case "GEM":
					if(stableBeams){
						return "GEM-collisions";
					}
					break;
			}
			return problemSubsystem;
		}
		return null;
	}
}