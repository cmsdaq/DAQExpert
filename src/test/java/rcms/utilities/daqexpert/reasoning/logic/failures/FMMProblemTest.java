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

		System.out.println("New message:");
		System.out.println(fmmProblem.getDescriptionWithContext());

		assertEquals(1, fmmProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("TRACKER", fmmProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		assertEquals(1, fmmProblem.getContext().getContext().get("PROBLEM-PARTITION").size());
		assertEquals("TEC-", fmmProblem.getContext().getContext().get("PROBLEM-PARTITION").iterator().next());

		assertEquals(1, fmmProblem.getContext().getContext().get("PROBLEM-FMM-URL").size());
		assertEquals("http://fmmpc-s1d12-07-01.cms:11100",
				fmmProblem.getContext().getContext().get("PROBLEM-FMM-URL").iterator().next());

		assertEquals(1, fmmProblem.getContext().getContext().get("PROBLEM-FMM-GEOSLOT").size());
		assertEquals(3, fmmProblem.getContext().getContext().get("PROBLEM-FMM-GEOSLOT").iterator().next());
	}

}
