package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

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

	private void test(String snapshotFile) throws URISyntaxException {

		DAQ snapshot = getSnapshot(snapshotFile);
		Map<String, Boolean> results = new HashMap<String, Boolean>();
		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);
		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(true, fc3.satisfied(snapshot, results));
		assertEquals(false, fc4.satisfied(snapshot, results));
		assertEquals(false, fc5.satisfied(snapshot, results));
	}

}
