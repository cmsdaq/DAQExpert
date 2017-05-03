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
public class FEDDisconnected extends DisconnectedAnalyzer {

	public FEDDisconnected() {
		this.name = "FED disconnected";
		this.description = "FED(s) {{PROBLEM-FED}} of {{PROBLEM-PARTITION}} partition of {{PROBLEM-SUBSYSTEM}} subsystem in disconnected state.";
		this.action = new SimpleAction(
				"Check with the {{PROBLEM-SUBSYSTEM}} subsystem DOC");
		
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		DisconnectedSubcase a = detectDisconnect(daq);
		if (a == DisconnectedSubcase.FEDDisconnected) {
			result = true;
		}

		return result;
	}
}
