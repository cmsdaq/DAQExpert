package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public abstract class KnownFailure extends ActionLogicModule {

	public KnownFailure() {
		this.setNotificationPlay(true);
		this.setNotificationDisplay(true);
		this.setPrefixToPlay("Known failure: ");
		this.setSuffixToPlay(". Steps ready");
		this.priority = ConditionPriority.CRITICAL;
		this.setSoundToPlay(Sound.KNOWN);
	}
}
