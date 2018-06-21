package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.CorruptedDataTest;

/**
 *
 * @author Maciej Gladki
 * 
 * @deprecated replaced by {@link CorruptedDataTest}
 */
@Deprecated
@Ignore
public class FlowchartCase2Test extends FlowchartCaseTestBase {

	@Test
	public void ecalSpecificCaseTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1478793337902.smile");

		assertEqualsAndUpdateResults(true, fc2, snapshot);

		// TODO: why ruFailed?
		assertEqualsAndUpdateResults(true, ruFailed, snapshot);
		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(true, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		//assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEquals(false, unidentified.satisfied(snapshot, results));
		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(644)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB+")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals(4, fc2.getActionWithContext().size());
	}

	@Test
	public void ecalSpecificCase2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498273936869.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(true, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		//assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEquals(false, unidentified.satisfied(snapshot, results));
		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(4, fc2.getActionWithContext().size());

	}

	@Test
	public void nonEcalTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498274256030.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(true, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		//assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);
		assertEqualsAndUpdateResults(false, fc5, snapshot);
		//assertEqualsAndUpdateResults(false, fc6, snapshot);
		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);
		assertEquals(false, unidentified.satisfied(snapshot, results));

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(833)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(3, fc2.getActionWithContext().size());
	}

	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		assertEqualsAndUpdateResults(true, fc2, snapshot);
		assertEqualsAndUpdateResults(true, ruFailed, snapshot);

		// TODO: why FC3 and FC6?
		assertEqualsAndUpdateResults(true, fc3, snapshot);
		//assertEqualsAndUpdateResults(true, fc6, snapshot);

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);
		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);
		assertEquals(false, unidentified.satisfied(snapshot, results));
		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(841, 843)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		// assertEquals(new HashSet(Arrays.asList("CSC+")),
		// contextHandler.getContextHandler().get("PROBLEM-PARTITION"));

		assertEquals(3, fc2.getActionWithContext().size());
	}

	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498551636794.smile");

		assertEqualsAndUpdateResults(true, fc2, snapshot);

		// TODO: why ruFailed?
		assertEqualsAndUpdateResults(true, ruFailed, snapshot);

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);
		assertEqualsAndUpdateResults(false, fc5, snapshot);
		//assertEqualsAndUpdateResults(false, fc6, snapshot);
		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);
		assertEquals(false, unidentified.satisfied(snapshot, results));

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals(4, fc2.getActionWithContext().size());
	}

}
