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

	@Test
	public void case1Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1478748186297.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("TTCP"));
	}

	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480813540739.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));
	}

	@Test
	public void ttsAtTopFMMNullButPmOutOfSyncTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1491576714151.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("MUTFUP")), context.getContext().get("TTCP"));
	}

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

	@Test
	public void case4Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1496647349638.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);

		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("TTCP"));
	}

	@Test
	public void case5Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		assertOnlyOneIsSatisified(fc3, snapshot);

		// TODO: why FC6 and FC2, ruFailde?

		Context context = fc3.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));

	}

}
