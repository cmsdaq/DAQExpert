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
	public void declareRelations(){
		require(LogicModuleRegistry.StableBeams);
		require(LogicModuleRegistry.ExpectedRate);
		require(LogicModuleRegistry.NoRate);

		declareAffected(LogicModuleRegistry.TTSDeadtime);
	}

	@Override
	public boolean satisfied(DAQ daq) {
		boolean stableBeams = false;
		boolean expectedRate = false;
		boolean noRate = false;

		stableBeams = getOutputOf(LogicModuleRegistry.StableBeams).getResult();
		expectedRate = getOutputOf(LogicModuleRegistry.ExpectedRate).getResult();
		noRate = getOutputOf(LogicModuleRegistry.NoRate).getResult();

		if (stableBeams)
			this.priority = ConditionPriority.CRITICAL;
		else
			this.priority = ConditionPriority.WARNING;

		if (expectedRate && noRate)
			return true;
		return false;
	}

}
