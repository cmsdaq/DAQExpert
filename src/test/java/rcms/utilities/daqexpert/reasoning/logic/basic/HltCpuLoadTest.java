package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private HltCpuLoad makeInstance(long runOngoingHoldOffPeriod, long selfHoldOffPeriod) {
		HltCpuLoad result = new HltCpuLoad();

		// mock properties
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD.getKey(), "0.9");
		properties.setProperty(Setting.EXPERT_LOGIC_HLT_CPU_LOAD_RUNONGOING_HOLDOFF_PERIOD.getKey(), "" + runOngoingHoldOffPeriod);
		properties.setProperty(Setting.EXPERT_LOGIC_HLT_CPU_LOAD_SELF_HOLDOFF_PERIOD.getKey(), "" + selfHoldOffPeriod);

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
		HltCpuLoad module = makeInstance(0, 0);

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

	/** test for the holdoff mechanism at the beginning of a run */
	@Test
	public void testHoldOffPeriod() {

		final int runOngoingHoldOffPeriod = 10;
		final int selfHoldOffPeriod = 1;

		HltCpuLoad module = makeInstance(runOngoingHoldOffPeriod, selfHoldOffPeriod);

		// prepare a sequence of events and expected results
		List<HltHoldOffTestData> sequence = new ArrayList<HltHoldOffTestData>();

		//                       timestamp, runOngoing, expectedResult
		sequence.add(new HltHoldOffTestData(0, false, false).setCpuLoad(0.5f));

		// high HLT load outside run - but self holdoff
		sequence.add(new HltHoldOffTestData(5, false, false).setCpuLoad(0.95f));

		// high HLT load outside run - and self holdoff ok
		sequence.add(new HltHoldOffTestData(7, false, true).setCpuLoad(0.95f));

		// run starts with reasonable CPU load
		sequence.add(new HltHoldOffTestData(10, true, false).setCpuLoad(0.5f));

		// too high CPU load but within holdoff period
		sequence.add(new HltHoldOffTestData(19, true, false).setCpuLoad(0.95f));

		// too high CPU load after holdoff period
		sequence.add(new HltHoldOffTestData(20, true, true).setCpuLoad(0.95f));

		// normal high CPU load after holdoff period
		sequence.add(new HltHoldOffTestData(30, true, false).setCpuLoad(0.5f));

		// again too high CPU load after holdoff period - but result false as self holdoff timer
		sequence.add(new HltHoldOffTestData(31, true, false).setCpuLoad(0.95f));

		// self holdoff timer now releasees
		sequence.add(new HltHoldOffTestData(35, true, true).setCpuLoad(0.95f));

		//-----

		Map<String, Output> results = new HashMap<>();

		for (HltHoldOffTestData data : sequence) {
			results.put(RunOngoing.class.getSimpleName(), new Output(data.isRunOngoing()));

			// needed for assigning priorities
			results.put(StableBeams.class.getSimpleName(), new Output(true));
			results.put(BackpressureFromHlt.class.getSimpleName(), new Output(true));

			DAQ snapshot = new DAQ();
			snapshot.setLastUpdate(data.getTimestamp());

			// for the moment we do not have a test case snapshot for this class
			// so we have to put high CPU load by hand
			HltInfo hltInfo = new HltInfo();
			hltInfo.setCpuLoad(data.getCpuLoad());
			snapshot.setHltInfo(hltInfo);

			// run module to be tested
			boolean result = module.satisfied(snapshot, results);

			assertEquals("test failed for snapshot at time " + data.getTimestamp(),
							data.isExpectedResult(), result);

		} // loop over test sequence

	}

}
