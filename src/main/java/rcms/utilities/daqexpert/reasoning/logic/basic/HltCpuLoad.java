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
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

/**
 * Logic module identifying high HLT cpu load.
 */
public class HltCpuLoad extends KnownFailure implements Parameterizable {

	private static final Logger logger = Logger.getLogger(HltCpuLoad.class);

	private Float maxCpuLoad;


	/**
	 *  Timer keeping condition of for a period after RunOngoing condition satisfied
	 */
	private HoldOffTimer runOngoingHoldOffTimer;


	/**
	 * Timer which keeping condition off for a period after self condition satisfied
	 */
	private HoldOffTimer selfHoldOffTimer;

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
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		assignPriority(results);

		// check if we are in a run now
		// (rely on the fact that RunOngoing has been run already)
		Boolean runOngoing = results.get(RunOngoing.class.getSimpleName()).getResult();

		boolean ignoreRunOngoingHoldoff = runOngoing ? false : true;

		long now = daq.getLastUpdate();

		// update the holdoff timer
		runOngoingHoldOffTimer.updateInput(runOngoing, now);

		boolean thresholdExceeded = false;

		Float cpuLoad = null;
		if (daq.getHltInfo() != null && daq.getHltInfo().getCpuLoad() != null) {
			cpuLoad = daq.getHltInfo().getCpuLoad();
		}


		if (cpuLoad != null && cpuLoad > maxCpuLoad) {
			thresholdExceeded = true;
			selfHoldOffTimer.updateInput(true, now);
		} else {
			selfHoldOffTimer.updateInput(false, now);
			return false;
		}

		// check all the conditions now
		boolean result = false;
		if (thresholdExceeded && ( ignoreRunOngoingHoldoff || (runOngoing && runOngoingHoldOffTimer.getOutput(now))) && selfHoldOffTimer.getOutput(now)) {
			contextHandler.registerForStatistics("HLT_CPU_LOAD", cpuLoad * 100, " %", 1);
			result =  true;
		}


		return result;

	}

	@Override
	public void parametrize(Properties properties) {
		this.maxCpuLoad = FailFastParameterReader.getFloatParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD, this.getClass());
		this.description = String.format("HLT CPU load is high ({{HLT_CPU_LOAD}}, which exceeds the threshold of %.1f%%. [[NOTE]]", maxCpuLoad * 100);

		// an integer in milliseconds is enough to describe 24 days of
		// holdoff period...
		Integer runOngoingHoldOffPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_RUNONGOING_HOLDOFF_PERIOD, this.getClass());
		Integer selfHoldOffPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_SELF_HOLDOFF_PERIOD, this.getClass());
		runOngoingHoldOffTimer = new HoldOffTimer(runOngoingHoldOffPeriod);
		selfHoldOffTimer = new HoldOffTimer(selfHoldOffPeriod);
	}

}
