package rcms.utilities.daqexpert.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;

public class RunComparator extends Comparator {

	public RunComparator() {
		this.name = "n/a";
		this.group = EventGroup.Run;
		this.priority = EventPriority.defaultt;
	}

	private static Logger logger = Logger.getLogger(RunComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (current.getRunNumber() != previous.getRunNumber()) {
			logger.debug("New run identified " + current.getRunNumber());
			this.name = "run number: " + current.getRunNumber();
			result = true;
		}
		return result;
	}

}
