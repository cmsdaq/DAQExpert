package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.SimpleContextEntry;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class ProblemWithPiTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1492094890109.smile");

		System.out.println(piProblem.getDescriptionWithContext());
		assertOnlyOneIsSatisified(piProblem, snapshot);

		assertEquals(1, ((SimpleContextEntry<String>)piProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM")).getObjectSet().size());
		assertEquals("ECAL", ((SimpleContextEntry<String>)piProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM")).getObjectSet().iterator().next());

		assertEquals(1, ((SimpleContextEntry<String>)piProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-PARTITION")).getObjectSet().size());
		assertEquals("EB+", ((SimpleContextEntry<String>)piProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-PARTITION")).getObjectSet().iterator().next());

	}

}
