package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class FEDDisconnectedTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1493157340132.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEquals(false, piDisconnected.satisfied(snapshot, results));
		assertEquals(false, piProblem.satisfied(snapshot, results));
		assertEquals(true, fedDisconnected.satisfied(snapshot, results));
		assertEquals(false, fmmProblem.satisfied(snapshot, results));
		
		
		assertEquals(false, fc5.satisfied(snapshot, results));
		assertEquals(false, fc6.satisfied(snapshot, results));
	}

}
