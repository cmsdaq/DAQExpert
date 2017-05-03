package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
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
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));
		
		// new subcases of old flowchart case 4
		assertEquals(false, piDisconnected.satisfied(snapshot, results));
		assertEquals(false, piProblem.satisfied(snapshot, results));
		assertEquals(false, fedDisconnected.satisfied(snapshot, results));
		assertEquals(false, fmmProblem.satisfied(snapshot, results));
		
		assertEquals(false, fc5.satisfied(snapshot, results));
		assertEquals(true, fc6.satisfied(snapshot, results));

	}

}
