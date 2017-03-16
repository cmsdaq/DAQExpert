package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class SubsystemSoftError extends ContextLogicModule {

	public SubsystemSoftError() {
		this.name = "Subsystem soft error detected";
		this.description = "{{SUBSYSTEM}} subsystem is in soft error detected";
		this.priority = ConditionPriority.DEFAULTT;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		if (!runOngoing)
			return false;
		
		boolean expectedRate = results.get(ExpectedRate.class.getSimpleName());
		if (!expectedRate)
			return false;
		
		boolean transition = results.get(LongTransition.class.getSimpleName());
		if (transition)
			return false;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {
			if ("RunningSoftErrordetected".equalsIgnoreCase(subSystem.getStatus())) {
				context.register("SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
