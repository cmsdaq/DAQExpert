package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RuStuckWaiting extends BackpressureAnalyzer {

	public RuStuckWaiting() {
		this.name = "RU waiting for other FED";

		this.description = "RU {{AFFECTED-RU}} is stuck waiting for FED {{PROBLEM-FED}}"
				+ "FED(s) belong(s) to TTCP {{PROBLEM-TTCP}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "Fragment count"
				+ "This causes backpressure at FED {{AFFECTED-FED}} in the same FED Builder";

		this.action = null;

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.WaitingForOtherFedsInFB) {
			result = true;
		}
		return result;
	}

}
