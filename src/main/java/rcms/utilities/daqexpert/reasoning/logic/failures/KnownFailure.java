package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.List;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;

/**
 * Abstract base class for logic-modules that implement specific known failures.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class KnownFailure extends ActionLogicModule {

	public KnownFailure() {
		this.priority = ConditionPriority.CRITICAL;
	}

	
	public String getDescriptionWithContext() {
		return this.getContext().getContentWithContext(this.description);
	}

	public List<String> getActionWithContext() {
		return this.getContext().getActionWithContext(this.action);
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
