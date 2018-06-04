package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RuStuck extends BackpressureAnalyzer {

	public RuStuck() {
		this.name = "RU stuck";

		this.description = "RU {{AFFECTED-RU}} is stuck. " + "RU has more than 0 requests - {{RU-REQUESTS}}. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";
		this.briefDescription = "RU {{AFFECTED-RU}} is stuck";

		this.action = null;

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

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.RuIsStuck) {
			result = true;
		}
		return result;
	}

}
