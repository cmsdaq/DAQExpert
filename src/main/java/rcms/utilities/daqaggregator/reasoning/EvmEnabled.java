package rcms.utilities.daqaggregator.reasoning;

import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;

public class EvmEnabled implements Condition {
	private final static Logger logger = Logger.getLogger(EvmEnabled.class);

	@Override
	public Boolean satisfied(DAQ daq) {
		List<FEDBuilder> a = daq.getFedBuilders();
		for (FEDBuilder b : a) {
			RU ru = b.getRu();
			if (ru.isEVM()) {
			}
		}
		boolean result = false;

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Warning;
	}

	@Override
	public String getText() {
		return EvmEnabled.class.getSimpleName();
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {
		// nothing to do

	}
	
	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}

}
