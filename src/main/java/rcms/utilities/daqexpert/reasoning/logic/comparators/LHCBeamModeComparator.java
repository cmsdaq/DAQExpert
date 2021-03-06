package rcms.utilities.daqexpert.reasoning.logic.comparators;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class LHCBeamModeComparator extends ComparatorLogicModule {

	public LHCBeamModeComparator() {
		this.name = "n/a";
		this.priority = ConditionPriority.DEFAULTT;
		this.description = "New LHC beam mode identified";
	}

	private static Logger logger = Logger.getLogger(LHCBeamModeComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcBeamMode().equals(previous.getLhcBeamMode())) {
			logger.debug("New LHC Beam mode " + new Date(current.getLastUpdate()));
			this.name = current.getLhcBeamMode();
			if (current.getLhcBeamMode().equalsIgnoreCase("Stable Beams"))
				this.priority = ConditionPriority.IMPORTANT;
			else
				this.priority = ConditionPriority.DEFAULTT;

			result = true;
		}
		return result;
	}

}
