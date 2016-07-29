package rcms.utilities.daqexpert.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class LHCBeamModeComparator extends Comparator {

	public LHCBeamModeComparator() {
		this.name = "n/a";
		this.group = EventGroup.LHC_BEAM;
		this.priority = EventPriority.defaultt;
		this.description = "New LHC beam mode identified";
	}

	private static Logger logger = Logger.getLogger(LHCBeamModeComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcBeamMode().equals(previous.getLhcBeamMode())) {
			logger.debug("New LHC Beam mode " + new Date(current.getLastUpdate()));
			this.name = current.getLhcBeamMode();
			if (current.getLhcBeamMode().equalsIgnoreCase("Stable Beams"))
				this.priority = EventPriority.important;
			else
				this.priority = EventPriority.defaultt;

			result = true;
		}
		return result;
	}

}
