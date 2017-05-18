package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

/**
 * Out of sequence test
 * 
 * @author holzner
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class OutOfSequenceTest extends FlowchartCaseTestBase {

	/**
	 * 
	 * Cannot detect with current logic. No FED is backpressured!
	 * 
	 * <a href=
	 * "http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2016-12-04-01:05:48">
	 * DAQView link</a>
	 */
	@Test
	public void testSatisfied() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1480809948643.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(548, fc1.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-RU").size());
		assertEquals("ru-c2e14-27-01.cms", fc1.getContext().getContext().get("PROBLEM-RU").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("ES", fc1.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-TTCP").size());
		assertEquals("ES+", fc1.getContext().getContext().get("PROBLEM-TTCP").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1360, fc1.getContext().getContext().get("AFFECTED-FED").iterator().next());

	}

	/**
	 * <a href=
	 * "http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2017-03-20-13:49:09">
	 * DAQView link</a>
	 */
	@Test
	public void trgFedCase() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1490014149195.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(1386, fc1.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-RU").size());
		assertEquals("ru-c2e12-40-01.cms", fc1.getContext().getContext().get("PROBLEM-RU").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1380, fc1.getContext().getContext().get("AFFECTED-FED").iterator().next());

	}

	/**
	 * <a href=
	 * "http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2016-11-26-07:21:35">
	 * DAQView link</a>
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void dtFedCase() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");
		assertOnlyOneIsSatisified(fc1, snapshot);

	}

	/**
	 * test parsing of the FED number from the RU error message for a few cases.
	 */
	@Test
	public void testFEDparsing() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493263275021.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(622, fc1.getContext().getContext().get("PROBLEM-FED").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("ECAL", fc1.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-TTCP").size());
		assertEquals("EB-", fc1.getContext().getContext().get("PROBLEM-TTCP").iterator().next());

	}

}
