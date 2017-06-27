package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class HLTProblem extends BackpressureAnalyzer {

	public HLTProblem() {
		this.name = "HLT problem";

		this.description = "All BUs are in Blocked or Cloud"
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		this.action = null;

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.HltProblem) {
			result = true;
		}
		return result;
	}

}
