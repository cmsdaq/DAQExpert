package rcms.utilities.daqaggregator.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class SessionComparator extends Comparator {

	private static Logger logger = Logger.getLogger(SessionComparator.class);

	private String sessionId;


	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;
		
		if (current.getSessionId() != previous.getSessionId()) {
			logger.debug("New session identified " + new Date(current.getLastUpdate()));
			sessionId = "session id: " + current.getSessionId();
			result = true;
		}
		return result;
	}


	@Override
	public String getText() {
		return sessionId;
	}
	@Override
	public Level getLevel() {
		return Level.Run;
	}
	
	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}


}
