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
public class FlowchartCase5Test extends FlowchartCaseTestBase {

	/* 2016-11-20T04:59:38 */
	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1479614378467.smile");
		assertOnlyOneIsSatisified(fc5, snapshot);
		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TEC-")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(169)), context.getContext().get("FED"));
	}

	/* http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-26-03:28:25 */
	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498440505470.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);
		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(149)), context.getContext().get("FED"));
	}

	/* http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-22-04:01:25 */
	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498096885568.smile");

		// FIXME: why ferol fifo stuck here?
		assertSatisfiedLogicModules(snapshot, fc5, ferolFifoStuck);

		System.out.println(fc5.getDescriptionWithContext());

		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(838)), context.getContext().get("FED"));

	}

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-15-23:29:34
	 */
	@Test
	public void case5Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497562174081.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);

		System.out.println(fc5.getDescriptionWithContext());

		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("HCAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("HBHEC")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(11114)), context.getContext().get("FED"));
	}

	/* http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-15-09:52:16 */
	@Test
	public void case4Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497513136376.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);
		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(83)), context.getContext().get("FED"));
	}

	
	
}
