package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.basic.helper.Time;
import rcms.utilities.daqexpert.reasoning.logic.basic.helper.WorkingHourResolver;

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

		this.action = new SimpleAction("Red recycle the DAQ (if in stable beams or outside extended working hours). Call DAQ on-call and ask him to dump FEROL / FEROL40 registers  during extended working hours and outside stable beams.");
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

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.LinkProblem) {
			result = true;
		}
        return result;
	}

}
