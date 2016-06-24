package rcms.utilities.daqexpert.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;

public class SessionComparator extends Comparator {

	public SessionComparator() {
		this.name = "n/a";
		this.group = EventGroup.Run;
		this.priority = EventPriority.defaultt;
	}

	private static Logger logger = Logger.getLogger(SessionComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (current.getSessionId() != previous.getSessionId()) {
			logger.debug("New session identified " + new Date(current.getLastUpdate()));
			name = "session id: " + current.getSessionId();
			result = true;
		}
		return result;
	}

}
