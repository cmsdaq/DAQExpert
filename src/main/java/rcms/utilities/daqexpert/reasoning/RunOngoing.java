package rcms.utilities.daqexpert.reasoning;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.EventPriority;

public class RunOngoing extends Condition {

	public RunOngoing() {
		this.name = "Run ongoing";
		this.group = EventGroup.Run;
		this.priority = EventPriority.defaultt;
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		for (SubSystem curr : daq.getSubSystems()) {

			/* check tcds subsystem state */
			if (curr.getName().equalsIgnoreCase("TCDS")) {// change to constant
				String tcdsStatus = curr.getStatus();

				/* tcds can be running, pausing, paused, hard resseting */
				if (tcdsStatus.equalsIgnoreCase("running") || tcdsStatus.equalsIgnoreCase("paused")
						|| tcdsStatus.equalsIgnoreCase("pausing") || tcdsStatus.equalsIgnoreCase("resuming")
						|| tcdsStatus.equalsIgnoreCase("TTCHardResettingFromRunning")
						|| tcdsStatus.equalsIgnoreCase("TTCHardResetting")
						|| tcdsStatus.equalsIgnoreCase("TTCResyncingFromRunning")) {
					String l0 = daq.getLevelZeroState();
					if (!l0.equalsIgnoreCase("stopping") && !l0.equalsIgnoreCase("halting")
							&& !l0.equalsIgnoreCase("forcestopping") && !l0.equalsIgnoreCase("forcehalting"))
						return true;
				}
			}
		}
		return false;
	}
}
