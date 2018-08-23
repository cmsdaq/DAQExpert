package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.TestBase;

public class ProblemWithPiTest {

	@Test
	public void test() throws URISyntaxException {

		TestBase tester = new TestBase();

		Map<String, Output> result = tester.runLogic("1492094890109.smile");

		LogicModuleRegistry piProblem = LogicModuleRegistry.PiProblem;

		Output output = result.get(piProblem.getLogicModule().getClass().getSimpleName());

		Assert.assertTrue(output.getResult());

		assertEquals(1, ((ObjectContextEntry<String>)output.getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM")).getObjectSet().size());
		assertEquals("ECAL", ((ObjectContextEntry<String>)output.getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM")).getObjectSet().iterator().next());

		assertEquals(1, ((ObjectContextEntry<String>)output.getContext().getContextEntryMap().get("PROBLEM-PARTITION")).getObjectSet().size());
		assertEquals("EB+", ((ObjectContextEntry<String>)output.getContext().getContextEntryMap().get("PROBLEM-PARTITION")).getObjectSet().iterator().next());

		Assert.assertEquals("PI problem: PI of EB+ partition in ECAL subsystem is seen as disconnected but the FMM input to the PI is not disconnected. This seems to be a problem with the PI",
				((ContextLogicModule)piProblem.getLogicModule()).getDescriptionWithContext());


		Assert.assertEquals(piProblem, tester.dominating.getLogicModule());

	}

}
