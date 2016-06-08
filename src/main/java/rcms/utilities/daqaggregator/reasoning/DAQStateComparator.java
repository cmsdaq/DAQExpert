package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class DAQStateComparator extends Comparator {

	private static Logger logger = Logger.getLogger(DAQStateComparator.class);

	private String runId;

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getDaqState().equals(previous.getDaqState())) {
			logger.debug("DAQ state " + current.getDaqState());
			runId = "DAQ state: " + current.getDaqState();
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
		return Level.DAQ;
	}

	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}

}
