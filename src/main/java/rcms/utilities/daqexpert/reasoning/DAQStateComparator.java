package rcms.utilities.daqexpert.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class DAQStateComparator extends Comparator {

	public DAQStateComparator() {
		this.name = "n/a";
		this.group = EventGroup.DAQ;
		this.priority = EventPriority.defaultt;
	}

	private static Logger logger = Logger.getLogger(DAQStateComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getDaqState().equals(previous.getDaqState())) {
			logger.debug("DAQ state " + current.getDaqState());
			this.name = "DAQ state: " + current.getDaqState();
			result = true;
		}
		return result;
	}

}
