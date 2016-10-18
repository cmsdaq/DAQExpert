package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Date;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * Identifies event minutes
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class EvenMinutesExample extends SimpleLogicModule {

	public EvenMinutesExample() {
		this.name = "even";
		this.description = "identifies even minutes";

		this.group = EventGroup.EXPERIMENTAL;
		this.priority = EventPriority.EXPERIMENTAL;
	}

	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Boolean> results) {

		Date snapshotDate = new Date(snapshot.getLastUpdate());
		if (snapshotDate.getMinutes() % 2 == 0) {
			return true;
		}
		return false;
	}

}
