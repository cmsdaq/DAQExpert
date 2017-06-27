package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class RuStuckTest extends FlowchartCaseTestBase {

	/**
	 * Remi reported this case on 10 April 2017:
	 * 
	 * MUFTUP FEDs 1384 and 1385 did not send a single event, which caused the
	 * RU to give backpressure to FED 1380
	 * 
	 * <a href=
	 * "http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-04-10-09:21:11">
	 * daqview</a>
	 * 
	 * FIXME: problem: fmm masked
	 */
	@Test
	public void remiscaseToConfirm() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491808871598.smile");

		assertOnlyOneIsSatisified(ruStuck, snapshot);

	}

	/**
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-
	 * 18-20:15:59
	 */
	@Test
	public void case1Test() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1495131324630.smile");

		assertOnlyOneIsSatisified(ruStuck, snapshot);

	}

	/**
	 * TODO: make sure this is related to ruStuck
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void devcase1Test() throws URISyntaxException {
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");

		// TODO: why FC1?

		assertOnlyOneIsSatisified(ruStuck, snapshot);

		Context context = ruStuck.getContext();
		assertEquals(new HashSet(Arrays.asList(773)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("DT")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("DT+")), context.getContext().get("TTCP"));
		assertEquals(new HashSet<>(Arrays.asList("DT")), context.getContext().get("FROZENSUBSYSTEM"));

	}

	/**
	 * TODO: make sure this is related to ruStuck
	 * 
	 * @throws URISyntaxException
	 */
	/**
	 * test to ensure that the subsystem which stopped sending fragments is
	 * correctly reported.
	 */
	@Test
	public void devcase2Test() throws URISyntaxException {

		// CEST: Fri, 9 Jun 2017 16:16:56 CEST
		// UTC: Fri, 9 Jun 2017 14:16:56 UTC
		DAQ snapshot = getSnapshot("1497017816236.smile");

		assertOnlyOneIsSatisified(ruStuck, snapshot);

		// check the subsystem reported as being at the origin of the problem
		// (whose FEDs stopped sending data)
		Context context = ruStuck.getContext();
		assertEquals(new HashSet(Arrays.asList(1404)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("GTUP")), context.getContext().get("TTCP"));
		assertEquals(new HashSet<>(Arrays.asList("PIXEL")), context.getContext().get("FROZENSUBSYSTEM"));

	}

	/**
	 * TODO: make sure this is related to ruStuck
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void devcase3Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1498067296082.smile");

		assertOnlyOneIsSatisified(ruStuck, snapshot);
		Context context = ruStuck.getContext();
		assertEquals(new HashSet(Arrays.asList(1386)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("MUTFUP")), context.getContext().get("TTCP"));
		assertEquals(new HashSet<>(Arrays.asList("TRG", "HF", "PIXEL")), context.getContext().get("FROZENSUBSYSTEM"));

	}

	/**
	 * TODO: make sure this is related to ruStuck
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void devcase4Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		assertOnlyOneIsSatisified(ruStuck, snapshot);
		// TODO: why FC3 and FC2, ruFailed

		Context context = ruStuck.getContext();
		assertEquals(new HashSet(Arrays.asList(1386)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("MUTFUP")), context.getContext().get("TTCP"));
		assertEquals(new HashSet<>(Arrays.asList("HF")), context.getContext().get("FROZENSUBSYSTEM"));
	}

}
