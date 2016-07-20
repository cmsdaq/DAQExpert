package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqaggregator.data.DAQ;

public abstract class Comparator extends EventFinder {

	private DAQ last;

	public boolean compare(DAQ daq) {
		boolean result = false;

		if (last != null) {
			try {
				result = compare(last, daq);
			} catch (NullPointerException e) {
				//e.printStackTrace();
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
