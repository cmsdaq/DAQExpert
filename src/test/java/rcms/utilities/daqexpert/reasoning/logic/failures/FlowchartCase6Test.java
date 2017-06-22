package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase6Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(true, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

	}

	/**
	 * test to ensure that the subsystem which stopped sending fragments is
	 * correctly reported.
	 */
	@Test
	public void case2Test() throws URISyntaxException {

		// CEST: Fri, 9 Jun 2017 16:16:56 CEST
		// UTC: Fri, 9 Jun 2017 14:16:56 UTC
		DAQ snapshot = getSnapshot("1497017816236.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(true, fc6, snapshot);

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

		// check the subsystem reported as being at the origin of the problem
		// (whose FEDs stopped sending data)
		assertEquals(new HashSet<>(Arrays.asList("PIXEL")), fc6.getContext().getContext().get("FROZENSUBSYSTEM"));

	}

	/**
	 * https://github.com/cmsdaq/DAQExpert/issues/79
	 */
	@Test
	public void case3Test() throws URISyntaxException {

		// CEST: Fri, 9 Jun 2017 16:16:56 CEST
		// UTC: Fri, 9 Jun 2017 14:16:56 UTC
		DAQ snapshot = getSnapshot("1498067623525.smile");

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(true, fc6, snapshot);

		System.out.println("FC6: " + fc6.getDescriptionWithContext());
		System.out.println("FC6: " + fc6.getActionWithContext());

		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

		// check the subsystem reported as being at the origin of the problem
		// (whose FEDs stopped sending data)
		assertEquals(new HashSet<>(Arrays.asList("PIXEL")), fc6.getContext().getContext().get("FROZENSUBSYSTEM"));

	}

}
