package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class SubsystemRunningDegraded extends ContextLogicModule {

	public SubsystemRunningDegraded() {
		this.name = "Subsystem running degraded";
		this.description = "{{SUBSYSTEM}} subsystem is in running degraded";
		this.priority = ConditionPriority.DEFAULTT;
		this.setNotificationPlay(true);
		this.setNotificationDisplay(true);
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
			if ("RunningDegraded".equalsIgnoreCase(subSystem.getStatus())) {
				context.register("SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
