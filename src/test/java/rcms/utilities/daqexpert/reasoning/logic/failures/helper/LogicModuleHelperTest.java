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

	@Test
	public void testGetFEDHierarchy() {

		FED f1 = new FED();
		FED f2 = new FED();
		FED f3 = new FED();
		FED f4 = new FED();
		FED f5 = new FED();

		f1.setSrcIdExpected(1);
		f2.setSrcIdExpected(2);
		f3.setSrcIdExpected(3);
		f4.setSrcIdExpected(4);
		f5.setSrcIdExpected(5);

		List<FED> feds = new ArrayList<FED>();
		feds.add(f1);
		feds.add(f2);
		feds.add(f3);
		feds.add(f4);
		feds.add(f5);

		TTCPartition p = new TTCPartition();
		p.setFeds(feds);

		/* All feds independent [[1:],[2:],[3:],[4:],[5:]] */
		Map<FED, Set<FED>> r1 = LogicModuleHelper.getFEDHierarchy(p);
		assertEquals(5, r1.size());
		assertEquals(0, r1.get(f1).size());
		assertEquals(0, r1.get(f2).size());
		assertEquals(0, r1.get(f3).size());
		assertEquals(0, r1.get(f4).size());
		assertEquals(0, r1.get(f5).size());

		/* put FED2 behind FED1 [[1:2],[3:],[4:],[5:]] */
		f2.setDependentFeds(Arrays.asList(f1));
		Map<FED, Set<FED>> r2 = LogicModuleHelper.getFEDHierarchy(p);
		assertEquals(4, r2.size());
		assertEquals(1, r2.get(f1).size());
		assertEquals("FED2 is no longer accessible as key", false, r2.containsKey(f2));
		assertEquals(0, r2.get(f3).size());
		assertEquals(0, r2.get(f4).size());
		assertEquals(0, r2.get(f5).size());

		/* put FED3 behind FED1 [[1: 3,2],[4:],[5:]] */
		f3.getDependentFeds().add(f1);
		Map<FED, Set<FED>> r3 = LogicModuleHelper.getFEDHierarchy(p);
		assertEquals(3, r3.size());
		assertEquals(2, r3.get(f1).size());
		assertEquals("FED2 is no longer accessible as key", false, r3.containsKey(f2));
		assertEquals("FED3 is no longer accessible as key", false, r3.containsKey(f3));
		assertEquals(0, r3.get(f4).size());
		assertEquals(0, r3.get(f5).size());

		/*
		 * put FED4 behind FED5 [[1: 3, 2],[5: 4]]
		 */
		f4.getDependentFeds().add(f5);
		Map<FED, Set<FED>> r4 = LogicModuleHelper.getFEDHierarchy(p);
		assertEquals(2, r4.size());
		assertEquals(2, r4.get(f1).size());
		assertEquals("FED2 is no longer accessible as key", false, r4.containsKey(f2));
		assertEquals("FED3 is no longer accessible as key", false, r4.containsKey(f3));
		assertEquals("FED4 is no longer accessible as key", false, r4.containsKey(f4));
		assertEquals(1, r4.get(f5).size());

		/*
		 * 
		 */

	}

}
