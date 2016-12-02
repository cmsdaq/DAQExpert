package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class SubsystemError extends ActionLogicModule {

	public SubsystemError() {
		this.name = "Subsystem in error";
		this.description = "{{SUBSYSTEM}} subsystem is in error";
		this.action = new SimpleAction("");
		this.group = EventGroup.Warning;
		this.priority = EventPriority.DEFAULTT;
		this.setNotificationPlay(true);
		this.setNotificationDisplay(true);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		if (!runOngoing)
			return false;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {
			if ("Error".equalsIgnoreCase(subSystem.getStatus())) {
				context.register("SUBSYSTEM", subSystem.getName());
				result = true;
			}
		}

		return result;
	}

}
