package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase6Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");


		assertEqualsAndUpdateResults(false, fc1,snapshot);
		assertEqualsAndUpdateResults(false, fc2,snapshot);
		assertEqualsAndUpdateResults(false, fc3,snapshot);
		
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, piProblem,snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem,snapshot);
		
		assertEqualsAndUpdateResults(false, fc5,snapshot);
		assertEqualsAndUpdateResults(true, fc6,snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

	}

}
