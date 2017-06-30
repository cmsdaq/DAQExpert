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
 * @author Maciej Gladki
 */
public class FlowchartCase3Test extends FlowchartCaseTestBase {

	/* 2016-11-10T04:23:06 */
	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1478748186297.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("TTCP"));
	}

	/* 2016-12-04T02:05:40 */
	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480813540739.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));
	}

	/* 2017-04-07T16:51:54 */
	@Test
	public void ttsAtTopFMMNullButPmOutOfSyncTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1491576714151.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("MUTFUP")), context.getContext().get("TTCP"));
	}

	/* 2017-06-20T14:56:45 */
	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497963405145.smile");

		assertOnlyOneIsSatisified(fc3, snapshot);

		// TODO: why FC1?
		System.out.println(fc1.getDescriptionWithContext());

		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("CTPPS_TOT")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TOTDET")), context.getContext().get("TTCP"));

	}

	/* 2017-06-05T09:22:29 */
	@Test
	public void case4Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1496647349638.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);

		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("TTCP"));
	}

	/*
	 * 2017-06-14T15:56:04
	 * 
	 * REMI: The message (ru-failed) is indeed redundant. However, the RU error message gives the details about the
	 * corruption. Is it possible to add the error message to the 'Corrupted data received' text?
	 * 
	 * HANNES: This is s strange case with multiple problems overlayed. It would be good to add the detailed error
	 * message of 3 also to 2. We should check if we can make RU-failed more specific so that it does not trigger in
	 * this case
	 * 
	 */
	@Test
	public void case5Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		// FIXME: ru failed redundant
		assertSatisfiedLogicModules(snapshot, fc2, fc3, ruFailed);

		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));

	}

}
