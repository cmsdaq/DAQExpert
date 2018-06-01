package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class PiDisconnected extends DisconnectedAnalyzer {

	public PiDisconnected() {
		this.name = "PI disconnected";
		this.description = "PI is disconnected. Pi of {{PROBLEM-PARTITION}} partition in {{PROBLEM-SUBSYSTEM}} subsystem is disconnected. This PI contains mTCA inputs for which no monitoring information is available. Cannot investigate further.";

		this.briefDescription = "Pi of {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}} is disconnected.";
		this.action = new SimpleAction(
				"Check the PI controller webpage to determine if this is the FED problem or a problem with the PI itself");
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {
		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;

		assignPriority(results);

		boolean result = false;

		DisconnectedSubcase a = detectDisconnect(daq);
		if (a == DisconnectedSubcase.PiDisconnected) {
			result = true;
		}

		return result;
	}
}
