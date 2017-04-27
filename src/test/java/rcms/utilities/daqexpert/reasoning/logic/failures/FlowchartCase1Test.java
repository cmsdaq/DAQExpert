package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Context;

/**
 *
 * @author holzner
 */
public class FlowchartCase1Test extends FlowchartCaseTestBase {

	/**
	 * Test of satisfied method, of class FlowchartCase1.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testSatisfied() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480809948643.smile");
		Map<String, Boolean> results = new HashMap();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)

		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(true, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));
		assertEquals(false, fc4.satisfied(snapshot, results));
		assertEquals(false, fc5.satisfied(snapshot, results));
		assertEquals(false, fc6.satisfied(snapshot, results));

	}

	/** test case with a different error message, originally
	 *  did not identify FED and TTC partition correctly.
	 */
	@Test
	public void testFEDparsing() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493263275021.smile");
		Map<String, Boolean> results = new HashMap();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)

		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(true, fc1.satisfied(snapshot, results));
		
		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("622")),  context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")),  context.getContext().get("TTCP"));
		
		//-----
		
		
	}
}
