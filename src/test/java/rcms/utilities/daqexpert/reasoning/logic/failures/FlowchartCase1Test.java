package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Context;

/**
 *
 * @author holzner
 */
public class FlowchartCase1Test extends FlowchartCaseTestBase {

	/**
	 * Test of satisfied method, of class FlowchartCase1.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testSatisfied() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480809948643.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertRestFalse(snapshot);

	}

	/**
	 * test parsing of the FED number from the RU error message for a few cases.
	 */
	@Test
	public void testFEDparsing() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493263275021.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertRestFalse(snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("622")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("TTCP"));

	}

	/**
	 * 
	 * another case with a different error message
	 * 
	 */
	@Test
	public void testFEDparsing2() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480809948643.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertRestFalse(snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("548")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("ES")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("ES+")), context.getContext().get("TTCP"));

	}

	@Test
	public void case03Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497898122474.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertRestFalse(snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("582")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("CTPPS_TOT")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TOTDET")), context.getContext().get("TTCP"));

	}

	@Test
	public void case04Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1496315027862.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertRestFalse(snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1326")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("FPIXM")), context.getContext().get("TTCP"));

	}

	@Test
	public void case05Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1495916283277.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertRestFalse(snapshot);

		System.out.println(fc1.getDescriptionWithContext());
		System.out.println(fc1.getActionWithContext());

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("1241")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("TTCP"));

	}

	private void assertRestFalse(DAQ snapshot) {
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, ruFailed, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);
		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(false, fc6, snapshot);
		assertEqualsAndUpdateResults(false, ferolFifoStuck, snapshot);
		assertEqualsAndUpdateResults(false, unidentified, snapshot);
	}
}
