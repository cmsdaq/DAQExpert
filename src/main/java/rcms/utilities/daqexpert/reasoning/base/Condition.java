package rcms.utilities.daqexpert.reasoning.base;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Base element of analysis results
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class Condition extends EventFinder {

	/**
	 * Is condition satisfied
	 * 
	 * @param daq
	 *            snapshot which is checked
	 * @param results
	 *            current results
	 * @return condition satisfied
	 */
	public abstract boolean satisfied(DAQ daq, Map<String, Boolean> results);

}
