package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class NoRateWhenExpected extends SimpleLogicModule {

	public NoRateWhenExpected() {
		this.name = "Dataflow stuck";
		this.priority = ConditionPriority.WARNING;
		this.description = "There is no rate when expected. The Data flow is stuck.";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		boolean stableBeams = false;
		boolean expectedRate = false;
		boolean noRate = false;
		boolean transition = false;

		stableBeams = results.get(StableBeams.class.getSimpleName());
		expectedRate = results.get(ExpectedRate.class.getSimpleName());
		noRate = results.get(NoRate.class.getSimpleName());

		if (stableBeams)
			this.priority = ConditionPriority.CRITICAL;
		else
			this.priority = ConditionPriority.WARNING;

		if (expectedRate && noRate)
			return true;
		return false;
	}

}
