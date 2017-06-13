package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class FMMProblemTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1491011351248.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, ruFailed, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(true, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

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
