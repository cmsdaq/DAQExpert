package rcms.utilities.daqexpert.reasoning.logic.failures;

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

		assertOnlyOneIsSatisified(unidentified, snapshot);

	}

}
