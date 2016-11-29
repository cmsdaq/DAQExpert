package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class NoRateWhenExpected extends SimpleLogicModule {

	public NoRateWhenExpected() {
		this.name = "No rate when expected";
		this.group = EventGroup.NO_RATE_WHEN_EXPECTED;
		this.priority = EventPriority.DEFAULTT;
		this.description = "No rate when expected";
		this.setNotificationDisplay(true);
		this.setNotificationPlay(true);
		this.setNotificationEndPlay(true);
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
		transition = results.get(Transition.class.getSimpleName());

		if (stableBeams)
			this.priority = EventPriority.CRITICAL;
		else
			this.priority = EventPriority.DEFAULTT;

		if (expectedRate && noRate && !transition)
			return true;
		return false;
	}

}
