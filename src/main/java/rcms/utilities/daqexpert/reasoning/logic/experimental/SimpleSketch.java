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
public class SimpleSketch extends SimpleLogicModule {

	/** TODO: 1. Set up basic information about this Logic Module here */
	public SimpleSketch() {
		this.name = "Sketch LM"; // TODO: 1a. set descriptive name
		this.description = "Easy start sketch LM"; // TODO 1b. provide more info

		this.group = EventGroup.EXPERIMENTAL; // leave it like this
		this.priority = EventPriority.defaultt; // leave it like this
	}

	/**
	 * TODO: 2. Implement condition of this LM in this method
	 * 
	 * @param snapshot
	 *            access any parameter of DAQ system using this object
	 * @param results
	 *            access results of other LMs using this map, name is the key
	 * @return return true when condition satisfied, false otherwise.
	 * 
	 */
	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Boolean> results) {

		/*
		 * TODO: 2a. implement your condition here
		 * 
		 * TODO: 2b. register your module at
		 * rcms.utilities.daqexpert.reasoning.processing.CheckManager:77
		 */
		return false;
	}

}
