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

		assertOnlyOneIsSatisified(piProblem, snapshot);

		assertEquals(1, piProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("ECAL", piProblem.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		assertEquals(1, piProblem.getContext().getContext().get("PROBLEM-PARTITION").size());
		assertEquals("EB+", piProblem.getContext().getContext().get("PROBLEM-PARTITION").iterator().next());

	}

}
