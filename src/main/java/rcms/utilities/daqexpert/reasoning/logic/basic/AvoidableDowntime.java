package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class AvoidableDowntime extends ActionLogicModule {

	public AvoidableDowntime() {
		this.name = "Avoidable Downtime";
		this.group = EventGroup.AVOIDABLE_DOWNTIME;
		this.priority = EventPriority.warning;
		this.description = "No rate and no recovery action is being executed during stable beams";
		this.action = null;
	}

	/**
	 * No rate when sum of FedBuilders rate equals 0 Hz
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		return results.get(NoRateWhenExpected.class.getSimpleName());

	}

}
