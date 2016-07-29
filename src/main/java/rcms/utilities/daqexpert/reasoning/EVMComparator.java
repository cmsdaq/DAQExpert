package rcms.utilities.daqexpert.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class EVMComparator extends Comparator {

	public EVMComparator() {
		this.name = "n/a";
		this.group = EventGroup.OTHER;
		this.priority = EventPriority.defaultt;
		this.description = "New EVM state identified";
	}

	private static Logger logger = Logger.getLogger(EVMComparator.class);

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;
		RU currentEVM = null;
		RU previousEVM = null;

		for (FEDBuilder a : current.getFedBuilders()) {
			RU ru = a.getRu();
			if (ru.isEVM())
				currentEVM = ru;
		}

		for (FEDBuilder a : previous.getFedBuilders()) {
			RU ru = a.getRu();
			if (ru.isEVM())
				previousEVM = ru;
		}
		if (currentEVM == null || previousEVM == null) {
			logger.error("EVM not found for shapshot " + new Date(current.getLastUpdate()));
			return false;
		}

		if (!currentEVM.getStatus().equals(previousEVM.getStatus())) {
			logger.debug("EVM state " + currentEVM.getStatus());
			this.name = "EVM state: " + currentEVM.getStatus();
			result = true;
		}
		return result;
	}

}
