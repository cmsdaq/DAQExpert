package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

public class UnidentifiedFailureTest extends FlowchartCaseTestBase {

	/**
	 * Test of unidentified failure
	 */
	@Test
	public void testSatisfied() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493872903036.smile");
		Map<String, Boolean> results = new HashMap();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)

		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		boolean fc1Result = fc1.satisfied(snapshot, results);
		assertEquals(false, fc1Result);
		results.put(fc1.getClass().getSimpleName(), fc1Result);

		boolean fc2Result = fc2.satisfied(snapshot, results);
		assertEquals(false, fc2Result);
		results.put(fc2.getClass().getSimpleName(), fc2Result);

		boolean fc3Result = fc3.satisfied(snapshot, results);
		assertEquals(false, fc3Result);
		results.put(fc3.getClass().getSimpleName(), fc3Result);

		boolean piDisconnectedResult = piDisconnected.satisfied(snapshot, results);
		assertEquals(false, piDisconnectedResult);
		results.put(piDisconnected.getClass().getSimpleName(), piDisconnectedResult);

		boolean piProblemResult = piProblem.satisfied(snapshot, results);
		assertEquals(false, piProblemResult);
		results.put(piProblem.getClass().getSimpleName(), piProblemResult);

		boolean fedDisconnectedResult = fedDisconnected.satisfied(snapshot, results);
		assertEquals(false, fedDisconnectedResult);
		results.put(fedDisconnected.getClass().getSimpleName(), fedDisconnectedResult);

		boolean fmmProblemResult = fmmProblem.satisfied(snapshot, results);
		assertEquals(false, fmmProblemResult);
		results.put(fmmProblem.getClass().getSimpleName(), fmmProblemResult);

		boolean fc5Result = fc5.satisfied(snapshot, results);
		assertEquals(false, fc5Result);
		results.put(fc5.getClass().getSimpleName(), fc5Result);

		boolean fc6Result = fc6.satisfied(snapshot, results);
		assertEquals(false, fc6Result);
		results.put(fc6.getClass().getSimpleName(), fc6Result);

		assertEquals(true, unidentified.satisfied(snapshot, results));

	}

}
