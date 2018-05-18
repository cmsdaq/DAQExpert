package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.HltInfo;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

/**
 * Test for class HltCpuLoad
 */
public class HltCpuLoadTest {

	private HltCpuLoad makeInstance(long holdOffPeriod) {
		HltCpuLoad result = new HltCpuLoad();

		// mock properties
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD.getKey(), "0.9");
		properties.setProperty(Setting.EXPERT_LOGIC_HLT_CPU_LOAD_HOLDOFF_PERIOD.getKey(), "" + holdOffPeriod);

		result.parametrize(properties);

		return result;
	}

	/**
	 * tests whether for the actual (mocked) cpu load the module fires or not as
	 * expected
	 */
	private void runTest(Float actualCpuLoad, boolean expectedResult) {

		Map<String, Output> results = new HashMap<>();

		results.put(RunOngoing.class.getSimpleName(), new Output(true));

		// needed for assigning priorities
		results.put(StableBeams.class.getSimpleName(), new Output(true));
		results.put(BackpressureFromHlt.class.getSimpleName(), new Output(true));

		// ensure that the RateTooHighTest module fires
		//
		// use a zero holdoff period so we don't
		// run into unexpected results due to being in the holdoff period
		// after the start of a run
		HltCpuLoad module = makeInstance(0);

		DAQ snapshot = new DAQ();

		// for the moment we do not have a test case snapshot for this class
		// so we have to put high CPU load by hand
		HltInfo hltInfo = new HltInfo();
		hltInfo.setCpuLoad(actualCpuLoad);
		snapshot.setHltInfo(hltInfo);

		// run module to be tested
		boolean result = module.satisfied(snapshot, results);

		assertEquals(expectedResult, result);
	}

	@Test
	public void testTooHighLoad() {
		runTest(0.95f, true);
	}

	@Test
	public void testOrdinaryLoad() {
		runTest(0.50f, false);
	}

	@Test
	public void testMissingLoad() {
		// mock the situation where the HLT CPU load could not be retrieved
		runTest(null, false);
	}
}
