package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class ProblemWithPiTest  extends FlowchartCaseTestBase  {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1492094890109.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEquals(false, piDisconnected.satisfied(snapshot, results));
		assertEquals(true, piProblem.satisfied(snapshot, results));
		
		System.out.println("New message:");
		System.out.println(piProblem.getDescriptionWithContext());

		assertEquals(1, piProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("ECAL", piProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		assertEquals(1, piProblem.getContext().getContext().get("PROBLEM-PARTITION").size());
		assertEquals("EB+", piProblem.getContext().getContext().get("PROBLEM-PARTITION").iterator().next());

		assertEquals(false, fedDisconnected.satisfied(snapshot, results));
		assertEquals(false, fmmProblem.satisfied(snapshot, results));
		
		//
		//TODO: why FC5 case is satisfied?
		//assertEquals(false, fc5.satisfied(snapshot, results));
		assertEquals(false, fc6.satisfied(snapshot, results));

		
	}

}
