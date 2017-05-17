package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
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
	@Ignore
	public void testSatisfied() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1480809948643.smile");
		Map<String, Boolean> results = new HashMap();

		// put results of prerequisite tests by hand
		// (as opposed to get them from a series of snapshots
		// which introduces a dependency on other tests)

		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(true, lfc1.satisfied(snapshot, results));
		System.out.println(lfc1.getDescriptionWithContext());

		assertEquals(true, fc1.satisfied(snapshot, results));
		System.out.println(fc1.getDescriptionWithContext());
		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(548, fc1.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-RU").size());
		assertEquals("ru-c2e14-27-01.cms", fc1.getContext().getContext().get("PROBLEM-RU").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1360, fc1.getContext().getContext().get("AFFECTED-FED").iterator().next());

		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEquals(false, fc5.satisfied(snapshot, results));
	}

	/**
	 * <a href=
	 * "http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2017-03-20-13:49:09">
	 * DAQView link</a>
	 */
	@Test
	public void trgFedCase() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1490014149195.smile");
		Map<String, Boolean> results = new HashMap();
		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(true, lfc1.satisfied(snapshot, results));
		System.out.println("Legacy message:");
		System.out.println(lfc1.getDescriptionWithContext());

		assertEquals(true, fc1.satisfied(snapshot, results));
		System.out.println("New message:");
		System.out.println(fc1.getDescriptionWithContext());
		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(1386, fc1.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc1.getContext().getContext().get("PROBLEM-RU").size());
		assertEquals("ru-c2e12-40-01.cms", fc1.getContext().getContext().get("PROBLEM-RU").iterator().next());

		assertEquals(1, fc1.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1380, fc1.getContext().getContext().get("AFFECTED-FED").iterator().next());

		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));
		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEquals(false, fc5.satisfied(snapshot, results));
		// assertEquals(false, fc6.satisfied(snapshot, results));
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
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(true, fc1.satisfied(snapshot, results));
		System.out.println(fc1.getDescriptionWithContext());
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEquals(false, fc5.satisfied(snapshot, results));

	}

}
