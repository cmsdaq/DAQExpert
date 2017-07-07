package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

/**
 * Tests for class LogicModuleHelper
 */
public class LogicModuleHelperTest {

	/**
	 * Test of getRefEventCounter method, of class LogicModuleHelper.
	 */
	// @Test
	public void testGetRefEventCounter() throws URISyntaxException {
		DAQ daq = FlowchartCaseTestBase.getSnapshot("1497017816236.smile");

		long tcdsEventCounter = LogicModuleHelper.getRefEventCounter(daq);

		assertEquals(35911, tcdsEventCounter);
	}

	/**
	 * Test of getFedsWithFewerFragments method, of class LogicModuleHelper.
	 */
	@Test
	public void testGetFedsWithFewerFragments() throws URISyntaxException {
		// PIXEL did not send any fragments
		DAQ daq = FlowchartCaseTestBase.getSnapshot("1497017816236.smile");

		// ----------

		List<Integer> expectedFedIds = new ArrayList<>();
		// find fedids of subsystem PIXEL
		for (SubSystem subsys : daq.getSubSystems()) {

			if (subsys.getName().equals("PIXEL")) {

				for (TTCPartition ttcp : subsys.getTtcPartitions()) {

					for (FED fed : ttcp.getFeds()) {

						if (fed.isHasSLINK() && !fed.isFrlMasked()) {
							expectedFedIds.add(fed.getSrcIdExpected());
						}

					} // loop over FEDs
				} // loop over TTC partitions

			} // if correct subsystem
		} // loop over subsystem

		Collections.sort(expectedFedIds);

		// ----------
		// run the method to be tested
		// ----------

		List<FED> actualFeds = LogicModuleHelper.getFedsWithFewerFragments(daq);

		List<Integer> actualFedIds = new ArrayList<>();

		for (FED fed : actualFeds) {

			// for the purpose of the test, ignore those which are backpressured
			// by the DAQ
			if (fed.getPercentBackpressure() <= 0) {

				actualFedIds.add(fed.getSrcIdExpected());

			}
		}

		Collections.sort(actualFedIds);

		assertEquals(expectedFedIds, actualFedIds);

	}

}
