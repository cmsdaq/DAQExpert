package rcms.utilities.daqexpert.reasoning.logic.comparators;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class LHCBeamModeComparator extends ComparatorLogicModule {

	public LHCBeamModeComparator() {
		this.name = "n/a";
		this.group = EventGroup.LHC_BEAM;
		this.priority = EventPriority.DEFAULTT;
		this.description = "New LHC beam mode identified";
		this.setNotificationPlay(true);
		this.setPrefixToPlay("Beam: ");
		this.setSoundToPlay(Sound.STATE_CHANGE_LHC_BEAM_MODE);
	}

	private static Logger logger = Logger.getLogger(LHCBeamModeComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcBeamMode().equals(previous.getLhcBeamMode())) {
			logger.debug("New LHC Beam mode " + new Date(current.getLastUpdate()));
			this.name = current.getLhcBeamMode();
			if (current.getLhcBeamMode().equalsIgnoreCase("Stable Beams"))
				this.priority = EventPriority.IMPORTANT;
			else
				this.priority = EventPriority.DEFAULTT;

			result = true;
		}
		return result;
	}

}
