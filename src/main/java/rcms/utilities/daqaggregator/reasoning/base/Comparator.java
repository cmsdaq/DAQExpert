package rcms.utilities.daqaggregator.reasoning.base;

import rcms.utilities.daqaggregator.data.DAQ;

public abstract class Comparator implements Classificable {

	private DAQ last;

	public boolean compare(DAQ daq) {
		boolean result = false;

		if (last != null) {
			result = compare(last, daq);
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
