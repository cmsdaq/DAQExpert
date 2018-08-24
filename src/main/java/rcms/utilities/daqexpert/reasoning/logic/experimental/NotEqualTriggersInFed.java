package rcms.utilities.daqexpert.reasoning.logic.experimental;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class NotEqualTriggersInFed extends SimpleLogicModule {

	public NotEqualTriggersInFed() {
		this.name = "Not equal triggers in fed";
		this.priority = ConditionPriority.DEFAULTT;
	}

	@Override
	public boolean satisfied(DAQ daq) {
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
