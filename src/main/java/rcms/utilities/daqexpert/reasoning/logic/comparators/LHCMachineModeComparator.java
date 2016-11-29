package rcms.utilities.daqexpert.reasoning.logic.comparators;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class LHCMachineModeComparator extends ComparatorLogicModule {

	public LHCMachineModeComparator() {
		this.name = "n/a";
		this.group = EventGroup.LHC_MACHINE;
		this.priority = EventPriority.DEFAULTT;
		this.description = "New LHC Machine mode identified";
		this.setNotificationPlay(true);
		this.setNotificationDisplay(false);
		this.setPrefixToPlay("Machine: ");
		this.setSoundToPlay(Sound.STATE_CHANGE_LHC_MACHINE_MODE);
	}

	private static Logger logger = Logger.getLogger(LHCMachineModeComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcMachineMode().equals(previous.getLhcMachineMode())) {
			logger.debug("New LHC Machine mode " + new Date(current.getLastUpdate()));
			this.name = current.getLhcMachineMode();
			result = true;
		}
		return result;
	}

}
