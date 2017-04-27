package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FMMProblem extends DisconnectedAnalyzer {

	public FMMProblem() {
		this.name = "FMM problem";
		
		this.description = "FMM problem: Top level FMM of the {{PROBLEM-PARTITION}} partition in {{PROBLEM-SUBSYSTEM}} subsystem with url {{PROBLEM-FMM-URL}} and geoslot {{PROBLEM-FMM-GEOSLOT}} has output state disconnected but no FED in that partition is disconnected.";

		this.action = new SimpleAction(
				"FMM crate may need to be powercycle. Call DAQ on call.");
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		DisconnectedSubcase a = detectDisconnect(daq);
		if (a == DisconnectedSubcase.FMMProblem) {
			result = true;
		}

		return result;
	}
}
