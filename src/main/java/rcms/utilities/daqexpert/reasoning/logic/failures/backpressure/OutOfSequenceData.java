package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.HashMap;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.HavingSpecialInstructions;

/**
 * Logic module identifying out of sequence data received (1st flowchart case)
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 * TODO: enable automation
 *
 */
public class OutOfSequenceData extends BackpressureAnalyzer implements HavingSpecialInstructions {

	public OutOfSequenceData() {
		this.name = "Out of sequence data received";

		this.description = "Run blocked by out-of-sync data from FED {{PROBLEM-FED}} received by RU {{PROBLEM-RU}} - now in syncloss state. "
				+ "Problem FED belongs to partition {{PROBLEM-PARTITION}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-PARTITION}} of {{AFFECTED-SUBSYSTEM}}";

		this.briefDescription = "Run blocked by out-of-sync data from FED(s) {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}}";


		/* Default action */
		ConditionalAction action = new ConditionalAction(
				true,
				"<<StopAndStartTheRun>> with <<RedAndGreenRecycle::{{PROBLEM-SUBSYSTEM}}>> using L0 Automator",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		/* SUBSYSTEM=Tracker action */
		// TODO: avoid automation here: 2 times, S/S only
		action.addContextSteps("TRACKER",
				"<<StopAndStartTheRun>> (try up to 2 times)",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		/* ecal specific case */
		// TODO: avoid automation: S/S only
		action.addContextSteps("ECAL", "<<StopAndStartTheRun>>",
				"If this doesn't help: <<StopAndStartTheRun>> with <<RedAndGreenRecycle::ECAL>>",
				"Call ECAL DOC during the Red Recycle (only if beam is not in RAMP mode)",
				"Problem not fixed: Call the DOC of ECAL");

		/* FED=1111 */
		// TODO: avoid automation: S/S only
		action.addContextSteps("FED1111or1109", "<<StopAndStartTheRun>>",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss)",
				"Problem fixed: Make an e-log entry."
						+ "Call the DOC {{PROBLEM-SUBSYSTEM}} (subsystem that caused the SyncLoss) to inform about the problem");

		/** GEM FED 1467 , see item 2 of issue #232 .
		 *  See also class LegacyFlowchartCase1
		 */
		// TODO: avoid automation: S/S only
		action.addContextSteps("GEM-1467",
						"<<StopAndStartTheRun>>",
						"Call the GEM DOC"
		);

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
		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
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

	@Override
	public String selectSpecialInstructionKey(DAQ daq, Map<String, Output> results) {


		String problemFED = contextHandler.getContextEntry("PROBLEM-FED").getTextRepresentation();
		String problemSubsystem = contextHandler.getContextEntry("PROBLEM-SUBSYSTEM").getTextRepresentation();
		boolean stableBeams = results.get(StableBeams.class.getSimpleName()).getResult();

		if(problemSubsystem!=null) {
			switch (problemSubsystem) {

				case "HCAL":
					if ("1111".equalsIgnoreCase(problemFED) || "1109".equalsIgnoreCase(problemFED)) {
						return "FED1111or1109";
					}
					break;
				case "GEM":
					if (stableBeams) {
						return "GEM-collisions";
					}
					if ( "1467".equalsIgnoreCase(problemFED)){
						return "GEM-1467";
					}
					break;
			}
			return problemSubsystem;
		}
		return null;
	}
}
