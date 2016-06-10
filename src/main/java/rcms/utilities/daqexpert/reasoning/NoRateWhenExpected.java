package rcms.utilities.daqexpert.reasoning;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Aware;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventClass;
import rcms.utilities.daqexpert.reasoning.base.Level;
import rcms.utilities.daqexpert.reasoning.states.LHCBeamMode;

public class NoRateWhenExpected extends Aware implements Condition {

	@Override
	public Level getLevel() {
		return Level.Error;
	}

	@Override
	public String getText() {
		return "No rate when expected";
	}

	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

	@Override
	public Boolean satisfied(DAQ daq) {
		boolean stableBeams = false;
		boolean runOngoing = false;
		boolean noRate = false;
		if (LHCBeamMode.STABLE_BEAMS == LHCBeamMode.getModeByCode(daq.getLhcBeamMode()))
			stableBeams = true;
		runOngoing = results.get(RunOngoing.class.getSimpleName());
		noRate = results.get(NoRate.class.getSimpleName());

		if (stableBeams && runOngoing && noRate)
			return true;
		return false;
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

	}

}
