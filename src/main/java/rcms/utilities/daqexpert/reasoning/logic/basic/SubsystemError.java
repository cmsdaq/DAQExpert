package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class SubsystemError extends ContextLogicModule {

	public SubsystemError() {
		this.name = "Subsystem in error";
		this.description = "{{PROBLEM-SUBSYSTEM}} subsystem is in error";
		this.priority = ConditionPriority.DEFAULTT;
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.RunOngoing);
		require(LogicModuleRegistry.ExpectedRate);
		require(LogicModuleRegistry.LongTransition);
	}


	@Override
	public boolean satisfied(DAQ daq) {

		boolean runOngoing =
				getOutputOf(LogicModuleRegistry.RunOngoing).getResult();
		if (!runOngoing)
			return false;
		
		boolean expectedRate = getOutputOf(LogicModuleRegistry.ExpectedRate).getResult();
		if (!expectedRate)
			return false;
		
		boolean transition = getOutputOf(LogicModuleRegistry.LongTransition).getResult();
		if (transition)
			return false;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {
			if ("Error".equalsIgnoreCase(subSystem.getStatus())) {
				contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
