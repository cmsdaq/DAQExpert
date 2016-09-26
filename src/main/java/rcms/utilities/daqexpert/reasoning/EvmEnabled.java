package rcms.utilities.daqexpert.reasoning;

import java.util.List;
import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class EvmEnabled extends Condition {

	public EvmEnabled() {
		this.name = "Enabled EVM";
		this.group = EventGroup.Warning;
		this.priority = EventPriority.defaultt;
		this.description = "EVM is enabled";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		List<FEDBuilder> a = daq.getFedBuilders();
		for (FEDBuilder b : a) {
			RU ru = b.getRu();
			if (ru.isEVM()) {
				//TODO: how to check if EVM is enabled? not masked?
			}
		}
		boolean result = false;

		return result;
	}

}
