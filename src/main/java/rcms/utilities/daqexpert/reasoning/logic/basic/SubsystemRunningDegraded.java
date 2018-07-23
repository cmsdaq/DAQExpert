package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class SubsystemRunningDegraded extends ActionLogicModule {

	public SubsystemRunningDegraded() {
		this.name = "Subsystem running degraded";
		this.description = "{{PROBLEM-SUBSYSTEM}} subsystem is in running degraded";
		this.priority = ConditionPriority.DEFAULTT;
		this.action = new SimpleAction("Subsytem {{PROBLEM-SUBSYSTEM}} is in RunningDegraded. " +
				"Please check the message in the subsystem information section in the Level-0 FM for subsystem {{PROBLEM-SUBSYSTEM}}. " +
				"It should contain a description of the problem. ",
				"Use your common sense to decide whether a human expert needs to look at the problem or whether CMS can continue taking data despite the problem. ",
				"If there is no problem description or if the description is unclear, call the DOC of subsystem {{PROBLEM-SUBSYSTEM}}.");
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
			if ("RunningDegraded".equalsIgnoreCase(subSystem.getStatus())) {
				contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
