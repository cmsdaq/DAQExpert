package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class HLTProblemTest extends FlowchartCaseTestBase {

	/*
	 * test case where BugInFilterfarm originally fired but HLTProblem
	 * should fire instead.
	 */
	@Test
	public void test() throws URISyntaxException {
		
		DAQ snapshot = getSnapshot("1505394430084.json.gz");

		// ensure that only HLTProblem fires
		assertOnlyOneIsSatisified(b2, snapshot);

	}

}
