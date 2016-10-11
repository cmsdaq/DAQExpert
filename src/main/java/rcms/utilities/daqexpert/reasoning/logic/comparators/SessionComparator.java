package rcms.utilities.daqexpert.reasoning.logic.comparators;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class SessionComparator extends Comparator {

	public SessionComparator() {
		this.name = "n/a";
		this.group = EventGroup.SESSION_NUMBER;
		this.priority = EventPriority.defaultt;
		this.description = "DAQ session";
	}

	private static Logger logger = Logger.getLogger(SessionComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (current.getSessionId() != previous.getSessionId()) {
			logger.debug("New session identified " + new Date(current.getLastUpdate()));
			name = "" + current.getSessionId();
			result = true;
		}
		return result;
	}

}
