package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
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
				+ "FED belongs to TTCP {{AFFECTED-TTCP}} in {{AFFECTED-SUBSYSTEM}} subsystem. "
				+ "FED is in {{AFFECTED-TTCP-STATE}} and stopped sending data.";

		this.action = null;

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
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
