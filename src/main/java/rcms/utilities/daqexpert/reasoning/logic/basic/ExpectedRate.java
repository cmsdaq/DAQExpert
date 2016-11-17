package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class ExpectedRate extends SimpleLogicModule {

	public ExpectedRate() {
		this.name = "Expected rate";
		this.group = EventGroup.EXPECTED_RATE;
		this.priority = EventPriority.DEFAULTT;
		this.description = "Expecting rate";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean tcdsRunning = false;
		boolean daqRunning = false;
		boolean l0Running = false;

		if (daq.getDaqState().equals("Running")) {
			daqRunning = true;
		} else {
			return false;
		}

		if (daq.getLevelZeroState().equals("Running")) {
			l0Running = true;
		} else {
			return false;
		}

		for (SubSystem curr : daq.getSubSystems()) {
			if (curr.getName().equals("TCDS")) {
				if (curr.getStatus().equalsIgnoreCase("Running")) {
					tcdsRunning = true;
					break;
				}
			}
		}

		if (tcdsRunning && daqRunning && l0Running)
			return true;
		return false;
	}
}
