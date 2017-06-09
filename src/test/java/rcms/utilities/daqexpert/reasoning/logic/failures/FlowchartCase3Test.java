package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase3Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {
		test("1478748186297.smile");
	}

	@Test
	public void case2Test() throws URISyntaxException {
		test("1480813540739.smile");
	}
	
	@Test
	public void ttsAtTopFMMNullButPmOutOfSyncTest() throws URISyntaxException {
		test("1491576714151.smile");
	}
	
	

	private void test(String snapshotFile) throws URISyntaxException {

		DAQ snapshot = getSnapshot(snapshotFile);
		assertEqualsAndUpdateResults(false, fc1,snapshot );
		assertEqualsAndUpdateResults(false, fc2,snapshot );
		assertEqualsAndUpdateResults(true, fc3,snapshot );

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected,snapshot );
		assertEqualsAndUpdateResults(false, piProblem,snapshot );
		assertEqualsAndUpdateResults(false, fedDisconnected,snapshot );
		assertEqualsAndUpdateResults(false, fmmProblem,snapshot );

		assertEqualsAndUpdateResults(false, fc5,snapshot );
		assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);
	}

}
