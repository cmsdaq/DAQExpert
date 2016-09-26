package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class NotEqualTriggersInFed extends Condition {

	public NotEqualTriggersInFed() {
		this.name = "Not equal triggers in fed";
		this.group = EventGroup.OTHER;
		this.priority = EventPriority.defaultt;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
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

}
