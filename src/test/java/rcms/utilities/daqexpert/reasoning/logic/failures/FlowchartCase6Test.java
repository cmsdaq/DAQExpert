package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 * 
 * @deprecated replaced by package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure
 */
@Deprecated
@Ignore
public class FlowchartCase6Test extends FlowchartCaseTestBase {


	@Test
	public void case1Test() throws URISyntaxException {
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");

		// TODO: why FC1?
		// there is actually one RU in syncloss
		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, ruFailed, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		// assertEqualsAndUpdateResults(true, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

		/*
		 * ContextHandler contextHandler = fc6.getContextHandler(); assertEquals(new HashSet(Arrays.asList(773)),
		 * contextHandler.getContextHandler().get("FED")); assertEquals(new HashSet(Arrays.asList("DT")),
		 * contextHandler.getContextHandler().get("SUBSYSTEM")); assertEquals(new HashSet(Arrays.asList("DT+")),
		 * contextHandler.getContextHandler().get("PROBLEM-PARTITION")); assertEquals(new HashSet<>(Arrays.asList("DT")),
		 * contextHandler.getContextHandler().get("FROZENSUBSYSTEM"));
		 */
	}

	private void assertLmsOutput(DAQ snapshot) {

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		// assertEqualsAndUpdateResults(true, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);
	}

	/**
	 * test to ensure that the subsystem which stopped sending fragments is correctly reported.
	 */
	@Test
	public void case2Test() throws URISyntaxException {

		// CEST: Fri, 9 Jun 2017 16:16:56 CEST
		// UTC: Fri, 9 Jun 2017 14:16:56 UTC
		DAQ snapshot = getSnapshot("1497017816236.smile");

		assertLmsOutput(snapshot);

		// check the subsystem reported as being at the origin of the problem
		// (whose FEDs stopped sending data)
		/*
		 * ContextHandler contextHandler = fc6.getContextHandler(); assertEquals(new HashSet(Arrays.asList(1404)),
		 * contextHandler.getContextHandler().get("FED")); assertEquals(new HashSet(Arrays.asList("TRG")),
		 * contextHandler.getContextHandler().get("SUBSYSTEM")); assertEquals(new HashSet(Arrays.asList("GTUP")),
		 * contextHandler.getContextHandler().get("PROBLEM-PARTITION")); assertEquals(new HashSet<>(Arrays.asList("PIXEL")),
		 * contextHandler.getContextHandler().get("FROZENSUBSYSTEM"));
		 */

	}

	@Test
	public void case3Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1498067296082.smile");

		assertLmsOutput(snapshot);

		/*
		 * ContextHandler contextHandler = fc6.getContextHandler(); assertEquals(new HashSet(Arrays.asList(1386)),
		 * contextHandler.getContextHandler().get("FED")); assertEquals(new HashSet(Arrays.asList("TRG")),
		 * contextHandler.getContextHandler().get("SUBSYSTEM")); assertEquals(new HashSet(Arrays.asList("MUTFUP")),
		 * contextHandler.getContextHandler().get("PROBLEM-PARTITION")); assertEquals(new HashSet<>(Arrays.asList("TRG", "HF", "PIXEL")),
		 * contextHandler.getContextHandler().get("FROZENSUBSYSTEM"));
		 */

	}

	@Test
	public void case4Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		assertEqualsAndUpdateResults(true, fc2, snapshot);
		assertEqualsAndUpdateResults(true, ruFailed, snapshot);

		// TODO: why FC3 and FC2?
		assertEqualsAndUpdateResults(true, fc3, snapshot);
		// assertEqualsAndUpdateResults(true, fc6, snapshot);

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);
		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);
		assertEquals(false, unidentified.satisfied(snapshot));

		/*
		 * ContextHandler contextHandler = fc6.getContextHandler(); assertEquals(new HashSet(Arrays.asList(1386)),
		 * contextHandler.getContextHandler().get("FED")); assertEquals(new HashSet(Arrays.asList("TRG")),
		 * contextHandler.getContextHandler().get("SUBSYSTEM")); assertEquals(new HashSet(Arrays.asList("MUTFUP")),
		 * contextHandler.getContextHandler().get("PROBLEM-PARTITION")); assertEquals(new HashSet<>(Arrays.asList("HF")),
		 * contextHandler.getContextHandler().get("FROZENSUBSYSTEM"));
		 */
	}
}
