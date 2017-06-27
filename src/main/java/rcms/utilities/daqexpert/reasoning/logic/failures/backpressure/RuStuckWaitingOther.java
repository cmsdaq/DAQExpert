package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RuStuckWaitingOther extends BackpressureAnalyzer {

	public RuStuckWaitingOther() {
		this.name = "RU waiting for other FED";

		this.description = "RU {{AFFECTED-RU}} is stuck waiting for FED {{PROBLEM-FED}}. "
				+ "Problem FED(s) belong(s) to partition {{PROBLEM-TTCP}} in {{PROBLEM-SUBSYSTEM}} subsystem. "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in other FED-builder {{AFFECTED-FED-BUILDER}}. "
				+ "Note that there is nothing wrong with backpressured FED {{AFFECTED-FED}}.";

		this.action = null;

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.BackpressuredByOtherFed) {
			result = true;
		}
		return result;
	}

}
