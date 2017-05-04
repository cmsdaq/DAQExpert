package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class ProblemWithPiTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1492094890109.smile");


		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(true, piProblem, snapshot);

		System.out.println("New message:");
		System.out.println(piProblem.getDescriptionWithContext());

		assertEquals(1, piProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("ECAL", piProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		assertEquals(1, piProblem.getContext().getContext().get("PROBLEM-PARTITION").size());
		assertEquals("EB+", piProblem.getContext().getContext().get("PROBLEM-PARTITION").iterator().next());

		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		//
		// TODO: why FC5 case is satisfied?
		// assertEqualsAndUpdateResults(false, fc5,snapshot );
		assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

	}

}
