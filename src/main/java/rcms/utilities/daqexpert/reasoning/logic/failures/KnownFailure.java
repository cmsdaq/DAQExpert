package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public abstract class KnownFailure extends ActionLogicModule {

	public KnownFailure() {
		this.priority = ConditionPriority.CRITICAL;
	}
}
