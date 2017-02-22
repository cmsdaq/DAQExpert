package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This logic module identifies no rate condition in DAQ
 */
public class LongTransition extends SimpleLogicModule {

	public LongTransition() {
		this.name = "LongTransition";
		this.group = EventGroup.HIDDEN;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Transition for new run (long)";
	}

	/**
	 * Transition time in ms
	 */
	private final int transitionTime = 30000;
	private int duration;
	private long started;

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean expectedRate = results.get(ExpectedRate.class.getSimpleName());

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
