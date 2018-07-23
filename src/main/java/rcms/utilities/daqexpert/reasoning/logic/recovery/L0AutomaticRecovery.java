package rcms.utilities.daqexpert.reasoning.logic.recovery;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This identifies when L0 is executing automatic recovery action
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class L0AutomaticRecovery extends SimpleLogicModule {

	public L0AutomaticRecovery() {
		this.name = "L0 automatic recovery";
		this.description = "L0 is currently executing automatic recovery action";
		this.priority = ConditionPriority.DEFAULTT;
		this.problematic = false;
	}

	private static List<String> states = Arrays.asList("FixingSoftError","TTCHardResetting", "TTCHardResettingFromRunning", "TTCResyncing", "TTCResyncingFromRunning", "PerformingDCSPauseResume");

	@Override
	public boolean satisfied(DAQ snapshot, Map<String, Output> results) {


		String levelZeroState = snapshot.getLevelZeroState();

		if(states.contains(levelZeroState)){
			return true;
		}
		return false;
	}

}
