package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Date;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This is an sketch of Logic Module (LM)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class SimpleSketchLogicModule extends SimpleLogicModule {

	/** TODO: 1. Set up basic information about this Logic Module here */
	public SimpleSketchLogicModule() {
		this.name = "Sketch LM"; // TODO: 1a. set descriptive name
		this.description = "Easy start sketch LM"; // TODO 1b. provide more info

		this.priority = ConditionPriority.DEFAULTT; // leave it like this
	}

	/**
	 * TODO: 2. Implement condition of this LM in this method. Return true when
	 * your condition is satisfied, otherwise return false.
	 * 
	 * @param snapshot
	 *            access any parameter of DAQ system using this object
	 * @return return true when condition satisfied, false otherwise.
	 * 
	 */
	@Override
	public boolean satisfied(DAQ snapshot) {

		/*
		 * TODO: 2a. implement your condition here
		 * 
		 * TODO: 2b. you can reuse other LMs results by accessing
		 * results.get(NoRate.class.getSimpleName())
		 * 
		 * TODO: 2c. register your module at
		 * rcms.utilities.daqexpert.reasoning.processing.CheckManager:77
		 */
		Date snapshotDate = new Date(snapshot.getLastUpdate());
		if(snapshotDate.getMinutes() %2 == 0){
			return true;
		}
		return false;
	}

}
