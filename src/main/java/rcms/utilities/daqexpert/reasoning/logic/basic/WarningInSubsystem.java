package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

@Deprecated
public class WarningInSubsystem extends ContextLogicModule {

	public WarningInSubsystem() {
		this.name = "Warning in partition";
		this.description = "TTCP {{PROBLEM-PARTITION}} of {{PROBLEM-SUBSYSTEM}} subsystem is in warning {{WARNING}}, it may affect rate.";
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

		boolean runOngoing = getOutputOf(LogicModuleRegistry.RunOngoing).getResult();

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

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() > 50F) {
					contextHandler.register("PROBLEM-PARTITION", ttcp.getName());
					contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());
					contextHandler.registerForStatistics("WARNING", ttcp.getPercentWarning(),"%",1);
					result = true;
				}
			}
		}

		return result;
	}

}
