package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

public abstract class KnownFailure extends ActionLogicModule {

	public KnownFailure() {
		this.priority = ConditionPriority.CRITICAL;
	}

	protected void assignPriority(Map<String, Boolean> results) {
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? ConditionPriority.CRITICAL : ConditionPriority.WARNING;
	}

	public String getDescriptionWithContext() {
		return this.getContext().getContentWithContext(this.description);
	}

	protected boolean isMasked(FED fed) {
		if (fed.isFmmMasked() || fed.isFrlMasked()) {
			return true;
		}
		return false;
	}

	protected TTSState getParitionState(TTCPartition partition) {
		String result = partition.getTtsState();

		if (result == null) {
			result = partition.getTcds_pm_ttsState();
		}
		if (result == null) {
			return TTSState.UNKNOWN;
		} else {
			return TTSState.getByCode(result);
		}
	}
}
