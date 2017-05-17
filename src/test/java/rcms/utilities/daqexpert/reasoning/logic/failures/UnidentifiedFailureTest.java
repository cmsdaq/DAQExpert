package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

public class UnidentifiedFailureTest extends FlowchartCaseTestBase {

	/**
	 * Test of unidentified failure
	 */
	@Test
	public void testSatisfied() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493872903036.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);

		assertEquals(true, unidentified.satisfied(snapshot, results));

	}

}
