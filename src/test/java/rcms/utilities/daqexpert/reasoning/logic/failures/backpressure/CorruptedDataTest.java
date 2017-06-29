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
 * Test corrupted data discovery
 * 
 * @author Maciej Gladki
 */
public class CorruptedDataTest extends FlowchartCaseTestBase {

	/**
	 * http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2017-03-27-15:52:23
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * @throws URISyntaxException
	 */
	@Ignore
	@Test
	public void ecalFedCorruptedDataTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1490622743834.smile");
		assertSatisfiedLogicModules(snapshot, fc2); // FC2 not identified with new BackpressureAnalyzer
		assertEquals(1, fc2.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(644, fc2.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc2.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1360, fc2.getContext().getContext().get("AFFECTED-FED").iterator().next());

	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-18-20:16:51
	 * 
	 * Ignored because it's impossible to identify it with new approach - there is no partitions in B/W
	 */
	@Test
	@Ignore
	public void case4Test() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1495131411780.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2016-11-10-16:55:37
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 */
	@Ignore
	@Test
	public void ecalSpecificCaseTest() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1478793337902.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

		// TODO: why ruFailed?

		assertEquals(false, unidentified.satisfied(snapshot, results));
		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		Context context = fc2.getContext();
		assertEquals(new HashSet(Arrays.asList(644)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB+")), context.getContext().get("TTCP"));

		assertEquals(4, fc2.getActionWithContext().size());
	}

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-24-05:12:16
	 */
	@Test
	public void ecalSpecificCase2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498273936869.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed, fc5);

		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		Context context = fc2.getContext();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-TTCP"));
		assertEquals(4, fc2.getActionWithContext().size());

	}

	@Test
	public void nonEcalTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498274256030.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

		Context context = fc2.getContext();
		assertEquals(new HashSet(Arrays.asList(833)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().get("PROBLEM-TTCP"));
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(3, fc2.getActionWithContext().size());
	}

	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		// NOTE multiple LMs satisfied
		assertSatisfiedLogicModules(snapshot, fc2, fc3, ruFailed);

		Context context = fc2.getContext();
		assertEquals(new HashSet(Arrays.asList(841, 843)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().get("PROBLEM-TTCP"));
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("PROBLEM-SUBSYSTEM"));

		assertEquals(3, fc2.getActionWithContext().size());
	}

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-27-10:20:36
	 */
	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498551636794.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, fc5, ruFailed);

		Context context = fc2.getContext();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-TTCP"));

		assertEquals(4, fc2.getActionWithContext().size());
	}

}
