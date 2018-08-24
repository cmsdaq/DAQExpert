package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RuStuckWaitingOther extends BackpressureAnalyzer {

	public RuStuckWaitingOther() {
		this.name = "RU waiting for other FED";

		this.description = "RU {{AFFECTED-RU}} is stuck waiting for FED {{PROBLEM-FED}}. "
				+ "Problem FED(s) belong(s) to partition {{PROBLEM-PARTITION}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in other FED-builder {{AFFECTED-FED-BUILDER}} of subsystem {{AFFECTED-SUBSYSTEM}}. "
				+ "Note that there is nothing wrong with backpressured FED {{AFFECTED-FED}}.";

		this.briefDescription = "RU {{AFFECTED-RU}} is stuck waiting for FED(s) {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}}";

		this.action = new SimpleAction("<<StopAndStartTheRun>> with <<RedAndGreenRecycle::{{PROBLEM-SUBSYSTEM}}>> (whose FED stopped sending data)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{PROBLEM-SUBSYSTEM}} (whose FED stopped sending data) to inform",
				"Problem not fixed: Call the DOC for the subsystem {{PROBLEM-SUBSYSTEM}} (whose FED stopped sending data)");

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq) {

		if (!getOutputOf(LogicModuleRegistry.NoRateWhenExpected).getResult())
			return false;

		boolean result = false;
		//assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.BackpressuredByOtherFed) {
			result = true;
		}
		return result;
	}

}
