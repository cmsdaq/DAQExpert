package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class WarningInSubsystem extends ActionLogicModule {

	public WarningInSubsystem() {
		this.name = "Exists TTCP with warning > 0%";
		this.description = "TTCP {{TTCP}} of {{SUBSYSTEM}} subsystem is in warning {{WARNING}}, it may affect rate.";
		this.action = new SimpleAction("No action");
		this.group = EventGroup.Warning;
		this.priority = EventPriority.DEFAULTT;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean runOngoing = results.get(RunOngoing.class.getSimpleName());

		if (!runOngoing)
			return false;

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() > 50F) {
					context.register("TTCP", ttcp.getName());
					context.register("SUBSYSTEM", subSystem.getName());
					context.register("WARNING", ttcp.getPercentWarning());
					result = true;
				}
			}
		}

		return result;
	}

}
