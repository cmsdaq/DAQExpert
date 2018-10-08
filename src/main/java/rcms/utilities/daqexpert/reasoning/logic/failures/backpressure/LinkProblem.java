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
public class LinkProblem extends BackpressureAnalyzer {

	public LinkProblem() {
		this.name = "Link problem";

		this.description = "Link problem detected. "
				+ "RU {{AFFECTED-RU}} is waiting for backpressured FED {{AFFECTED-FED}}. "
				+ "FED belongs to TTCP {{AFFECTED-PARTITION}} in {{AFFECTED-SUBSYSTEM}} subsystem. "
				+ "FED is in {{AFFECTED-TTCP-STATE}} and stopped sending data.";

		this.briefDescription = "Link problem detected on RU {{AFFECTED-RU}}";
		this.action = new SimpleAction("Call the DAQ DOC during extended working hours to take a dump of the FEROL registers");

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
		if (backpressureRootCase == Subcase.LinkProblem) {
			result = true;
		}
		return result;
	}

}
