package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
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
public class OutOfSequenceDataTest extends FlowchartCaseTestBase {

	/*
	 * 
	 * THis issue is discussed in #84 - waiting for decisiont from #87
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-19-20:48:42
	 * 
	 * cannot identify as there is no backpressured FED
	 * 
	 */
	//@Ignore // cannot be identified with new logic
	@Test
	public void testTheDecisionWhatToDoWithRedundantRuFailedTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497898122474.smile");
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);

		//FIXME: fc1 should fire here
		assertSatisfiedLogicModules(snapshot, legacyFc1);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		Context context = legacyFc1.getContext();
		assertEquals(new HashSet(Arrays.asList("582")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("CTPPS_TOT")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TOTDET")), context.getContext().get("PROBLEM-TTCP"));
		

		assertEquals("CTPPS_TOT",legacyFc1.getContext().getActionKey());
		assertEquals(4,legacyFc1.getActionWithContext().size());

	}


	/**
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * 
	 * <a href= "http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2016-12-04-01:05:48"> DAQView
	 * link</a>
	 */
	@Test
	@Ignore
	public void testSatisfied() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
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
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-03-20-13:49:09
	 */
	@Ignore
	@Test
	public void trgFedCase() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1490014149195.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1386")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ES")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("ES+")), context.getContext().get("PROBLEM-TTCP"));
		assertEquals(new HashSet(Arrays.asList("ru-c2e12-40-01.cms")), context.getContext().get("PROBLEM-RU"));
		assertEquals(new HashSet(Arrays.asList(1380)), context.getContext().get("AFFECTED-FED"));
	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2016-11-26-07:21:35
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void dtFedCase() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");
		assertOnlyOneIsSatisified(fc1, snapshot);

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList(774)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("DT")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("DT+")), context.getContext().get("PROBLEM-TTCP"));
		assertEquals(new HashSet(Arrays.asList("ru-c2e15-28-01.cms")), context.getContext().get("PROBLEM-RU"));
		assertEquals(new HashSet(Arrays.asList(773)), context.getContext().get("AFFECTED-FED"));
		

		assertEquals("DT",fc1.getContext().getActionKey());
		assertEquals(4,fc1.getActionWithContext().size());

	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-04-27-05:21:15
	 */
	@Test
	public void testFEDparsing() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493263275021.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-TTCP"));

		assertEquals("ECAL",fc1.getContext().getActionKey());
		assertEquals(4,fc1.getActionWithContext().size());

	}

	/////////////////////////////////////////////////////////////

	/*
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-01-13:03:47
	 */
	@Test
	@Ignore
	public void fromDevTase04Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1496315027862.smile");
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);

		assertOnlyOneIsSatisified(fc1, snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1326")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("FPIXM")), context.getContext().get("PROBLEM-TTCP"));
		

		assertEquals("PIXEL",fc1.getContext().getActionKey());
		assertEquals(4,fc1.getActionWithContext().size());

	}

	/*
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-27-22:18:03
	 */
	@Test
	@Ignore
	public void fromDevTase05Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1495916283277.smile");

		assertOnlyOneIsSatisified(fc1, snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1241")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("PROBLEM-TTCP"));
		

		assertEquals("PIXEL", fc1.getContext().getActionKey());
		assertEquals(4,fc1.getActionWithContext().size());

	}

	/** testcase to check proper filling of {{PROBLEM-FED}}, {{PROBLEM-TTCP}}
	 * and {{PROBLEM-SUBSYSTEM}} (see issue #90)
	 *
	 */
	@Test
	public void testProblemFedFilling01() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1499843690396.json.gz");

		assertOnlyOneIsSatisified(fc1, snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList(1111)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("HCAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("HBHEB")), context.getContext().get("PROBLEM-TTCP"));

		assertEquals("FED1111or1109",fc1.getContext().getActionKey());
		assertEquals(3,fc1.getActionWithContext().size());

  }

}
