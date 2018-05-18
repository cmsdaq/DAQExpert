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

	private String additionalNote = "Note that there is also backpressure from HLT.";

	/** timer which returns true only if we are in a run and
	 *  after the holdoff period
	 */
	private HoldOffTimer holdOffTimer;

	public HltCpuLoad() {
		this.name = "HLT CPU load";
		this.action = new SimpleAction("Call the HLT DOC, mentioning the HLT CPU load is high. ");
	}

	@Override
	public void declareRequired(){
		require(LogicModuleRegistry.BackpressureFromHlt);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		assignPriority(results);

		// check if we are in a run now
		// (rely on the fact that RunOngoing has been run already)
		Boolean runOngoing = results.get(RunOngoing.class.getSimpleName()).getResult();

		// update the holdoff timer
		long now = daq.getLastUpdate();
		holdOffTimer.updateInput(runOngoing, now);

		if (runOngoing && !holdOffTimer.getOutput(now)) {
			// we are in a run but still in the holdoff period
			return false;
		}

		// we are either outside a run or after the holdoff period in the run

		if (daq.getHltInfo() == null) {
			return false;
		}

		Float cpuLoad = daq.getHltInfo().getCpuLoad();

		// TODO: should we issue a warning if we can not retrieve the 
		//       HLT cpu load ?
		if (cpuLoad == null) {
			return false;
		}

		boolean result = false;
		if (cpuLoad > maxCpuLoad) {
			contextHandler.registerForStatistics("HLT_CPU_LOAD", cpuLoad * 100, " %", 1);
			result =  true;
		}

		if (results.get(BackpressureFromHlt.class.getSimpleName()).getResult()) {
			//mention the fact that some modules are active
			contextHandler.registerConditionalNote("NOTE", additionalNote);
		} else{
			contextHandler.unregisterConditionalNote("NOTE");
		}

		return result;

	}

	@Override
	public void parametrize(Properties properties) {
		this.maxCpuLoad = FailFastParameterReader.getFloatParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD, this.getClass());
		this.description = String.format("HLT CPU load is high ({{HLT_CPU_LOAD}}, which exceeds the threshold of %.1f%%. [[NOTE]]", maxCpuLoad * 100);

		// an integer in milliseconds is enough to describe 24 days of
		// holdoff period...
		Integer holdOffPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_HLT_CPU_LOAD_HOLDOFF_PERIOD, this.getClass());
		holdOffTimer = new HoldOffTimer(holdOffPeriod);

	}

}
