package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * This is an sketch of Logic Module (LM)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class SketchLogicModule extends SimpleLogicModule {

	/** TODO: 1. Set up basic information about this Logic Module here */
	public SketchLogicModule() {
		this.name = "experimental"; // TODO: 1a. set descriptive name
		this.description = "Easy start sketch LM"; // TODO 1b. provide more info
		this.group = EventGroup.EXPERIMENTAL;
		this.priority = EventPriority.EXPERIMENTAL;
	}

	/**
	 * TODO: 2. Implement condition of this LM here. Return true when
	 * your condition is satisfied, otherwise return false.
	 * 
	 * @param snapshot
	 *            access any parameter of DAQ system using this object
	 * @param results
	 *            you can reuse other LMs results by accessing this object
	 *            e.g. results.get(NoRate.class.getSimpleName())
	 * @return return true when condition satisfied, false otherwise.
	 * 
	 */
	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Boolean> results) {

		/* TODO: 2a. implement your condition here */
		return false;
	}

}
