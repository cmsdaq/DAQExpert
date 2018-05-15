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
		this.description = "{{SUBSYSTEM}} subsystem is in error";
		this.priority = ConditionPriority.DEFAULTT;
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.RunOngoing);
		require(LogicModuleRegistry.ExpectedRate);
		require(LogicModuleRegistry.LongTransition);
	}


	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName()).getResult();
		if (!runOngoing)
			return false;
		
		boolean expectedRate = results.get(ExpectedRate.class.getSimpleName()).getResult();
		if (!expectedRate)
			return false;
		
		boolean transition = results.get(LongTransition.class.getSimpleName()).getResult();
		if (transition)
			return false;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {
			if ("Error".equalsIgnoreCase(subSystem.getStatus())) {
				contextHandler.register("SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
