package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ReusableContextEntry;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class FMMProblemTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1491011351248.smile");

		System.out.println("New message:");
		System.out.println(fmmProblem.getDescriptionWithContext());
		assertOnlyOneIsSatisified(fmmProblem, snapshot);

		assertEquals(1, ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM")).getObjectSet().size());
		assertEquals("TRACKER", ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM")).getObjectSet().iterator().next());

		assertEquals(1, ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-PARTITION")).getObjectSet().size());
		assertEquals("TEC-", ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-PARTITION")).getObjectSet().iterator().next());

		assertEquals(1, ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FMM-URL")).getObjectSet().size());
		assertEquals("http://fmmpc-s1d12-07-01.cms:11100",
				((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FMM-URL")).getObjectSet().iterator().next());

		assertEquals(1, ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FMM-GEOSLOT")).getObjectSet().size());
		assertEquals(3, ((ReusableContextEntry<String>)fmmProblem.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FMM-GEOSLOT")).getObjectSet().iterator().next());
	}

}
