package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Tests for class FEROLFifoStuckTest . Note that some of the test cases are very close in time and thus may actually be
 * the same problem after starting/stopping etc.
 * 
 */
public class FEROLFifoStuckTest extends FlowchartCaseTestBase {

	@Test
	public void test01() throws URISyntaxException {
		// Sat, 3 Jun 2017 06:41:27 CEST
		// Sat, 3 Jun 2017 04:41:27 UTC
		// actually, TCDS is in paused here so in the future we
		// should not expect rate for this snapshot
		DAQ snapshot = getSnapshot("1496464887925.smile");
		assertOnlyOneIsSatisified(ferolFifoStuck, snapshot);
	}

	@Test
	public void test02() throws URISyntaxException {
		// Sat, 3 Jun 2017 06:20:00 CEST
		// Sat, 3 Jun 2017 04:20:00 UTC
		DAQ snapshot = getSnapshot("1496463600386.smile");
		assertOnlyOneIsSatisified(ferolFifoStuck, snapshot);
	}

	@Test
	public void test03() throws URISyntaxException {
		// Sat, 3 Jun 2017 05:55:00 CEST
		// Sat, 3 Jun 2017 03:55:00 UTC
		DAQ snapshot = getSnapshot("1496462100235.smile");
		assertOnlyOneIsSatisified(ferolFifoStuck, snapshot);
	}

	@Test
	public void test04() throws URISyntaxException {
		// Sat, 3 Jun 2017 05:43:59 CEST
		// Sat, 3 Jun 2017 03:43:59 UTC
		DAQ snapshot = getSnapshot("1496461439700.smile");
		assertOnlyOneIsSatisified(ferolFifoStuck, snapshot);
	}

	@Test
	public void test05() throws URISyntaxException {
		// Sat, 3 Jun 2017 05:39:00 CEST
		// Sat, 3 Jun 2017 03:39:00 UTC
		DAQ snapshot = getSnapshot("1496461140693.smile");
		assertOnlyOneIsSatisified(ferolFifoStuck, snapshot);
	}

	/**
	 * test case where this module should NOT fire
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-17-14:44:51
	 * 
	 * this snapshot should NOT fire FEROLFifoStuck
	 */
	@Test
	public void test06() throws URISyntaxException {
		// Sun, 17 Jun 2017 14:44:52 CEST
		// Sun, 17 Jun 2017 12:44:520 UTC

		DAQ snapshot = getSnapshot("1497703492467.smile");
		assertOnlyOneIsSatisified(fc5, snapshot);

		// TODO: make sure FC1 fires correctly here

	}

}
