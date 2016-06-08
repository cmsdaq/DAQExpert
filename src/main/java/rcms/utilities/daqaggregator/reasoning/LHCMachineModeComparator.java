package rcms.utilities.daqaggregator.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class LHCMachineModeComparator extends Comparator {

	private static Logger logger = Logger.getLogger(LHCMachineModeComparator.class);

	private String machineMode;


	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcMachineMode().equals(previous.getLhcMachineMode())) {
			logger.debug("New LHC Machine mode " + new Date(current.getLastUpdate()));
			machineMode = "Machine mode: " + current.getLhcMachineMode();
			result = true;
		}
		return result;
	}


	@Override
	public String getText() {
		return machineMode;
	}
	@Override
	public Level getLevel() {
		return Level.LHC;
	}
	
	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}


}
