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
public class BugInFilterfarm extends BackpressureAnalyzer {

	public BugInFilterfarm() {
		this.name = "Problem in filterfarm";

		this.description = "No requests on RUs and all BUs are in other states than enabled. There are no crashes of HLT processes."
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		this.briefDescription = "Problem in Filter Farm";

		this.action = new SimpleAction("Call the DAQ on-call.");

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);

		declareAffected(LogicModuleRegistry.TTSDeadtime);
		declareAffected(LogicModuleRegistry.BackpressureFromHlt);
	}

	@Override
	public boolean satisfied(DAQ daq) {



		if (!getOutputOf(LogicModuleRegistry.NoRateWhenExpected).getResult())
			return false;

		boolean result = false;
		//assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase ==Subcase.BugInFilterfarm) {
			result = true;
		}
		return result;
	}

}
