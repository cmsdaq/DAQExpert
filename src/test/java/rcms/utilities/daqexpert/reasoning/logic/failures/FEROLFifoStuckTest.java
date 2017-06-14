package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Tests for class FEROLFifoStuckTest . Note that some of the test cases are
 * very close in time and thus may actually be the same problem after
 * starting/stopping etc.
 * 
 */
public class FEROLFifoStuckTest extends FlowchartCaseTestBase {

	@Test
	public void test01() throws URISyntaxException {
		// Sat, 3 Jun 2017 06:41:27 CEST
		// Sat, 3 Jun 2017 04:41:27 UTC
		// actually, TCDS is in paused here so in the future we
		// should not expect rate for this snapshot
		test("1496464887925.smile", true);
	}

	@Test
	public void test02() throws URISyntaxException {
		// Sat, 3 Jun 2017 06:20:00 CEST
		// Sat, 3 Jun 2017 04:20:00 UTC
		test("1496463600386.smile", false);
	}

	@Test
	public void test03() throws URISyntaxException {
		// Sat, 3 Jun 2017 05:55:00 CEST
		// Sat, 3 Jun 2017 03:55:00 UTC
		test("1496462100235.smile", false);
	}

	@Test
	public void test04() throws URISyntaxException {
		// Sat, 3 Jun 2017 05:43:59 CEST
		// Sat, 3 Jun 2017 03:43:59 UTC
		test("1496461439700.smile", false);
	}

	@Test
	public void test05() throws URISyntaxException {
		// Sat, 3 Jun 2017 05:39:00 CEST
		// Sat, 3 Jun 2017 03:39:00 UTC
		test("1496461140693.smile", false);
	}

	private void test(String snapshotFile, boolean ignoreFc6) throws URISyntaxException {

		DAQ snapshot = getSnapshot(snapshotFile);

		assertEqualsAndUpdateResults(false, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);

		// the FEROL fifo stuck case actually looks very similar to FC6
		// but the event counters on the FEROL match those on the TCDS FED
		// (which they don't for FC6)
		//
		// For the moment we ignore whether FlowchartCase6 fires or not and rely
		// on the fact that FEROLFifoStuck has higher usefulness i.e. will be
		// displayed with higher priority
		//
		if (!ignoreFc6)
			assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(true, ferolFifoStuck, snapshot);

		// check that the FEROL controller URL is correct
		Set<Object> fullURLs = ferolFifoStuck.getContext().getContext().get("FRLFULLURL");
		assertEquals(
				new HashSet<>(Arrays
						.asList("http://frlpc40-s2d19-40-01.cms:11100/urn:xdaq-application:lid=111/expertDebugPage")),
				fullURLs);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);
	}

}
