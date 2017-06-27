package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Context;
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

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("548")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ES")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("ES+")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList("ru-c2e14-27-01.cms")), context.getContext().get("PROBLEM-RU"));
		assertEquals(new HashSet(Arrays.asList(1360)), context.getContext().get("AFFECTED-FED"));

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

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

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-TTCP"));

	}

	/////////////////////////////////////////////////////////////

	@Test
	public void fromDevTase03Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497898122474.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("582")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("CTPPS_TOT")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TOTDET")), context.getContext().get("TTCP"));

	}

	@Test
	public void fromDevTase04Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1496315027862.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1326")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("FPIXM")), context.getContext().get("TTCP"));

	}

	@Test
	public void fromDevTase05Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1495916283277.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1241")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("TTCP"));

	}

}
