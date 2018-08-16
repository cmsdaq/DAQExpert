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
public class OnlyFedStoppedSendingData extends BackpressureAnalyzer {

	public OnlyFedStoppedSendingData() {
		this.name = "FED stopped sending data";

		this.description = "The only FED {{PROBLEM-FED}} in RU {{PROBLEM-RU}} stopped sending data. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		this.briefDescription = "FED {{PROBLEM-FED}} stopped sending data.";

		this.action = new SimpleAction("Red recycle the {{PROBLEM-SUBSYSTEM}}",
						"Contact {{PROBLEM-SUBSYSTEM}} on-call expert in the meantime");;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.SpecificFedBlocking) {
			result = true;
		}
		return result;
	}

}
