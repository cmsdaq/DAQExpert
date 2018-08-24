package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies avoidable downtime condition in DAQ
 */
public class Downtime extends SimpleLogicModule {

	public Downtime() {
		this.name = "Downtime";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "No rate during stable beams";
		this.problematic = false; // this module is not problematic - it's for statistical reasons. No rate and dataflow stuck cover the analysis part
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRate);
		require(LogicModuleRegistry.StableBeams);

	}

	/**
	 * Avoidable downtime when downtime and no action being executed
	 */
	@Override
	public boolean satisfied(DAQ daq) {

		boolean noRate = getOutputOf(LogicModuleRegistry.NoRate).getResult();
		boolean stableBeams = getOutputOf(LogicModuleRegistry.StableBeams).getResult();

		if (stableBeams && noRate)
			return true;
		else
			return false;
	}

}
