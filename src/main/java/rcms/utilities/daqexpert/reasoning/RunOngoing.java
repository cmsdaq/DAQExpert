package rcms.utilities.daqexpert.reasoning;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.EventRaport;
import rcms.utilities.daqexpert.reasoning.base.Level;

public class RunOngoing implements Condition {

	@Override
	public Boolean satisfied(DAQ daq) {

		for (SubSystem curr : daq.getSubSystems()) {

			/* check tcds subsystem state */
			if (curr.getName().equalsIgnoreCase("TCDS")) {// change to constant
				String tcdsStatus = curr.getStatus();
				
				/* tcds can be running, pausing, paused, hard resseting */
				if (tcdsStatus.equalsIgnoreCase("running") || tcdsStatus.equalsIgnoreCase("paused")
						|| tcdsStatus.equalsIgnoreCase("pausing")
						|| tcdsStatus.equalsIgnoreCase("resuming")
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

	@Override
	public Level getLevel() {
		return Level.Run;
	}

	@Override
	public String getText() {
		return "Run ongoing";
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {
		EventRaport a = entry.getEventRaport();
		if (a.isInitialized())
			a.initialize("Run ongoing", "Run ongoing when tcds is running", "n/a");
	}

	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}
}
