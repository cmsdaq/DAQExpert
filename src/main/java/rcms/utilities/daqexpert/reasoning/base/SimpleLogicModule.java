package rcms.utilities.daqexpert.reasoning.base;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Simple type of Logic Module. It checks if the condition is satisfied.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class SimpleLogicModule extends LogicModule {

	/**
	 * Is condition of Logic Module satisfied. *
	 * 
	 * @param daq
	 *            Snapshot to examine by this Logic Module
	 * @param results
	 *            Results from other Logic Modules for this DAQ snapshot.
	 * @return condition satisfied, <code>true</code> when satisfied,
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean satisfied(DAQ daq, Map<String, Boolean> results);

}
