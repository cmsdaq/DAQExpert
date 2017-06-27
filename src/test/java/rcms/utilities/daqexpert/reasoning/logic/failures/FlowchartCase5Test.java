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

	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1479614378467.smile");
		assertOnlyOneIsSatisified(fc5, snapshot);
		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TEC-")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(169)), context.getContext().get("FED"));
	}

	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498440505470.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);
		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(149)), context.getContext().get("FED"));
	}

	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498096885568.smile");

		// TODO: why ferol fifo stuck here?
		assertOnlyOneIsSatisified(fc5, snapshot);

		Context context = fc5.getContext();
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().get("TTCP"));
		assertEquals(new HashSet(Arrays.asList(838)), context.getContext().get("FED"));
	}

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
