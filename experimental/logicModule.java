package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

/**
 * This is an sketch of Logic Module (LM)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class StableBeams extends SimpleLogicModule {

	/** TODO: 1. Set up basic information about this Logic Module here */
	public StableBeams() {
		this.name = "Stable beams"; // TODO: 1a. set descriptive name
		this.description = "Stable beams identified"; // TODO 1b. provide more
														// info

		this.group = EventGroup.EXPERIMENTAL; // leave it like this
		this.priority = EventPriority.EXPERIMENTAL; // leave it like this
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
	if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(snapshot.getLhcBeamMode()))
			return true;
		return false;
	}

}
