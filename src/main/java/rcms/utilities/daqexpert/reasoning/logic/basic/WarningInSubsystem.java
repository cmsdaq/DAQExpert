package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class WarningInSubsystem extends ContextLogicModule {

	public WarningInSubsystem() {
		this.name = "Warning in partition";
		this.description = "TTCP {{TTCP}} of {{SUBSYSTEM}} subsystem is in warning {{WARNING}}, it may affect rate.";
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
