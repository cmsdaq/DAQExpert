package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

public class FMMProblemTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491011351248.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));
		assertEquals(true, fmmProblem.satisfied(snapshot, results));
		assertEquals(false, fc5.satisfied(snapshot, results));
	}

}
