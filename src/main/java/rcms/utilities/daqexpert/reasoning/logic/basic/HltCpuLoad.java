package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.reasoning.logic.basic.helper.HoldOffTimer;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.BeginningOfRunHoldOffLogic;

/**
 * Logic module identifying high HLT cpu load.
 */
public class HltCpuLoad extends KnownFailure implements Parameterizable {

	private static final Logger logger = Logger.getLogger(HltCpuLoad.class);

	private Float maxCpuLoad;

	/** combined holdoff logic: combines beginning of run holdoff and above
	 *  threshold holdoff
	 */
	private BeginningOfRunHoldOffLogic holdOffLogic;

	public HltCpuLoad() {
		this.name = "HLT CPU load";
		this.action = new SimpleAction("Call the HLT DOC, mentioning the HLT CPU load is high. ");
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.BackpressureFromHlt);
		declareAffected(LogicModuleRegistry.BackpressureFromHlt);
	}

	@Override
	public boolean satisfied(DAQ daq) {

		//assignPriority(results);

		// check if we are in a run now
		// (rely on the fact that RunOngoing has been run already)
		Boolean runOngoing = getOutputOf(LogicModuleRegistry.RunOngoing).getResult();

		long now = daq.getLastUpdate();

		Float cpuLoad = null;
		if (daq.getHltInfo() != null && daq.getHltInfo().getCpuLoad() != null) {
			cpuLoad = daq.getHltInfo().getCpuLoad();
		}

		// update the holdoff logic
		holdOffLogic.updateInput(runOngoing, now, cpuLoad);

		boolean result = false;

		if (holdOffLogic.satisfied()) {
			// only update the statistics when the condition is met
			// (CPU load above threshold and holdoffs expired)
			contextHandler.registerForStatistics("HLT_CPU_LOAD", cpuLoad * 100, " %", 1);
			result =  true;
		}

		return result;
	}

	@Override
	public void parametrize(Properties properties) {
		this.maxCpuLoad = FailFastParameterReader.getFloatParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD, this.getClass());
		this.description = String.format("HLT CPU load is high ({{HLT_CPU_LOAD}}, which exceeds the threshold of %.1f%%.", maxCpuLoad * 100);

		// an integer in milliseconds is enough to describe 24 days of
		// holdoff period...
		Integer runOngoingHoldOffPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_RUNONGOING_HOLDOFF_PERIOD, this.getClass());
		Integer selfHoldOffPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_SELF_HOLDOFF_PERIOD, this.getClass());
		holdOffLogic = new BeginningOfRunHoldOffLogic(maxCpuLoad, runOngoingHoldOffPeriod, selfHoldOffPeriod);
	}

}
