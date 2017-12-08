package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class NoRateWhenExpected extends SimpleLogicModule {

	public NoRateWhenExpected() {
		this.name = "Dataflow stuck";
		this.priority = ConditionPriority.WARNING;
		this.description = "There is no rate when expected. The Data flow is stuck.";
	}

	@Override
	public void declareRequired(){
		require(LogicModuleRegistry.StableBeams);
		require(LogicModuleRegistry.ExpectedRate);
		require(LogicModuleRegistry.NoRate);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {
		boolean stableBeams = false;
		boolean expectedRate = false;
		boolean noRate = false;
		boolean transition = false;

		stableBeams = results.get(StableBeams.class.getSimpleName()).getResult();
		expectedRate = results.get(ExpectedRate.class.getSimpleName()).getResult();
		noRate = results.get(NoRate.class.getSimpleName()).getResult();

		if (stableBeams)
			this.priority = ConditionPriority.CRITICAL;
		else
			this.priority = ConditionPriority.WARNING;

		if (expectedRate && noRate)
			return true;
		return false;
	}

}
