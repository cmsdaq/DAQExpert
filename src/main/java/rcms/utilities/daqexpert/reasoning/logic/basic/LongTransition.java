package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class LongTransition extends SimpleLogicModule {

	public LongTransition() {
		this.name = "LongTransition";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "Transition for new run (long)";
		this.problematic = false;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.ExpectedRate);
	}

	/**
	 * Transition time in ms
	 */
	private final int transitionTime = 30000;
	private int duration;
	private long started;

	@Override
	public boolean satisfied(DAQ daq) {

		boolean expectedRate = getOutputOf(LogicModuleRegistry.ExpectedRate).getResult();

		// first check
		if (started == 0) {
			started = daq.getLastUpdate();
		} else {
			duration = (int) (daq.getLastUpdate() - started);
		}

		if (expectedRate) {
			if (duration < transitionTime)
				// transition time
				return true;
			else {
				// transition time passed but run is still ongoing
				return false;
			}
		} else {
			// run is not ongoing, reset the checker
			started = 0;
			duration = 0;
			return false;
		}
	}

}
