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
public class ProblemWithPi extends DisconnectedAnalyzer {

	public ProblemWithPi() {
		this.name = "PI problem";
		this.description = "PI problem: PI of {{PROBLEM-PARTITION}} partition in {{PROBLEM-SUBSYSTEM}} subsystem is seen as disconnected but the FMM input to the PI is not disconnected. This seems to be a problem with the PI";
		this.briefDescription = "PI of {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}} is seen as disconnected but the FMM input to the PI is not disconnected.";
		this.action = new SimpleAction("<<StopAndStartTheRun>> with <<RedAndGreenRecycle::{{PROBLEM-SUBSYSTEM}}>>",
				"If this doesn't help call the DAQ on-call");

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);

		declareAffected(LogicModuleRegistry.NoRateWhenExpected);
	}

	@Override
	public boolean satisfied(DAQ daq) {
		if (!getOutputOf(LogicModuleRegistry.NoRateWhenExpected).getResult())
			return false;

		//assignPriority(results);

		boolean result = false;

		DisconnectedSubcase a = detectDisconnect(daq);
		if (a == DisconnectedSubcase.ProblemWithPi) {
			result = true;
		}

		return result;
	}
}
