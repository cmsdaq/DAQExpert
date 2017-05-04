package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase2Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1478793337902.smile");

		assertEqualsAndUpdateResults(false, fc1,snapshot);
		assertEqualsAndUpdateResults(true, fc2,snapshot);
		assertEqualsAndUpdateResults(false, fc3,snapshot);
		
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, piProblem,snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem,snapshot);
		
		
		assertEqualsAndUpdateResults(false, fc5,snapshot);
		assertEqualsAndUpdateResults(false, fc6,snapshot);
		assertEquals(false, unidentified.satisfied(snapshot, results));


	}

}
