package rcms.utilities.daqexpert.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.Level;

public class NotEqualTriggersInFed implements Condition {
	private final static Logger logger = Logger.getLogger(NotEqualTriggersInFed.class);

	@Override
	public Boolean satisfied(DAQ daq) {
		boolean result = false;

		Long fedTriggers = null;
		for (FMMApplication fmmApplication : daq.getFmmApplications()) {
			for (FMM fmm : fmmApplication.getFmms()) {
				for (FED fed : fmm.getFeds()) {

					if (fedTriggers == null)
						fedTriggers = fed.getNumTriggers();
					if (fed.getNumTriggers() != fedTriggers) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Error;
	}

	@Override
	public String getText() {
		return NotEqualTriggersInFed.class.getSimpleName();
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
