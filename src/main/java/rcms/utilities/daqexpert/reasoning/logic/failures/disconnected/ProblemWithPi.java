package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ProblemWithPi extends DisconnectedAnalyzer {

	public ProblemWithPi() {
		this.name = "PI problem";
		this.description = "Problem with PI or its input in partition {{TTCP}} in {{SUBSYSTEM}} subsystem. ";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		DisconnectedSubcase a = detectDisconnect(daq);
		if (a == DisconnectedSubcase.ProblemWithPi) {
			result = true;
		}

		return result;
	}
}
