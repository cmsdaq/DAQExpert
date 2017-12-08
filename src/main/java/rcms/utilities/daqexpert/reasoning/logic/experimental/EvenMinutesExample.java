package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Date;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * Demo Logic Module: Identifies even minutes
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class EvenMinutesExample extends SimpleLogicModule {

	public EvenMinutesExample() {
		this.name = "even";
		this.description = "identifies even minutes";

		this.priority = ConditionPriority.EXPERIMENTAL;
	}

	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Output> results) {

		Date snapshotDate = new Date(snapshot.getLastUpdate());
		if (snapshotDate.getMinutes() % 2 == 0) {
			return true;
		}
		return false;
	}

}
