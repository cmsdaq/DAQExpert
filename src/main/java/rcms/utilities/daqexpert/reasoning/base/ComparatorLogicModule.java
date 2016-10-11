package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Type of Logic Module which identifies state transitions.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ComparatorLogicModule extends LogicModule {

	private DAQ last;

	public boolean compare(DAQ daq) {
		boolean result = false;

		if (last != null) {
			try {
				result = compare(last, daq);
			} catch (NullPointerException e) {
				// e.printStackTrace();
			}
		}

		last = daq;
		return result;
	}

	public abstract boolean compare(DAQ previous, DAQ current);

	public DAQ getLast() {
		return last;
	}

	public void setLast(DAQ last) {
		this.last = last;
	}

}
