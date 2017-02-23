package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class AvoidableDowntime extends SimpleLogicModule {

	public AvoidableDowntime() {
		this.name = "Avoidable Downtime";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "No rate and no recovery action is being executed during stable beams";
	}

	/**
	 * No rate when sum of FedBuilders rate equals 0 Hz
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? ConditionPriority.WARNING : ConditionPriority.DEFAULTT;
		

		return results.get(NoRateWhenExpected.class.getSimpleName());

	}

}
