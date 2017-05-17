package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class PiDisconnectedTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1490969849528.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);
		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(true, piDisconnected, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

		System.out.println("New message:");
		System.out.println(piDisconnected.getDescriptionWithContext());

		// assertEqualsAndUpdateResults(1,
		// fmmProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		// assertEqualsAndUpdateResults("TRACKER",
		// fmmProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

	}

}
