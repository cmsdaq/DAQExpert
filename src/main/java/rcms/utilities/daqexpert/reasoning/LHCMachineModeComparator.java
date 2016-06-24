package rcms.utilities.daqexpert.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;

public class LHCMachineModeComparator extends Comparator {

	public LHCMachineModeComparator() {
		this.name = "n/a";
		this.group = EventGroup.LHC;
		this.priority = EventPriority.defaultt;
	}

	private static Logger logger = Logger.getLogger(LHCMachineModeComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcMachineMode().equals(previous.getLhcMachineMode())) {
			logger.debug("New LHC Machine mode " + new Date(current.getLastUpdate()));
			this.name = "Machine mode: " + current.getLhcMachineMode();
			result = true;
		}
		return result;
	}

}
