package rcms.utilities.daqaggregator.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class EVMComparator extends Comparator {

	private static Logger logger = Logger.getLogger(EVMComparator.class);

	private String message;

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
		if(currentEVM == null || previousEVM == null){
			logger.error("EVM not found for shapshot " + new Date(current.getLastUpdate()));
			return false;
		}

		if (!currentEVM.getStatus().equals(previousEVM.getStatus())) {
			logger.debug("EVM state " + currentEVM.getStatus());
			message = "EVM state: " + currentEVM.getStatus();
			result = true;
		}
		return result;
	}

	@Override
	public String getText() {
		return message;
	}

	@Override
	public Level getLevel() {
		return Level.Info;
	}
	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}

}
