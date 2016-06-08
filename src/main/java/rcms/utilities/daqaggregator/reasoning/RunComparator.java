package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class RunComparator extends Comparator {

	private static Logger logger = Logger.getLogger(RunComparator.class);

	private String runId;


	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (current.getRunNumber() != previous.getRunNumber()) {
			logger.debug("New run identified " + current.getRunNumber());
			runId = "run number: " + current.getRunNumber();
			result = true;
		}
		return result;
	}


	@Override
	public String getText() {
		return runId;
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
