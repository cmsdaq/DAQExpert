package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;

public class RateOutOfRange implements Condition {
	private final static Logger logger = Logger.getLogger(RateOutOfRange.class);

	@Override
	public Boolean satisfied(DAQ daq) {
		float a = daq.getFedBuilderSummary().getRate();

		boolean result = false;
		if (50000 > a)
			result = true;

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Info;
	}

	@Override
	public String getText() {
		return RateOutOfRange.class.getSimpleName();
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {
		// nothing to do
	}
	
	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}

}
