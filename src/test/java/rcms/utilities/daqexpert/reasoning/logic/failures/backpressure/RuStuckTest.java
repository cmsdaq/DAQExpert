package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class RuStuckTest extends FlowchartCaseTestBase {

	/**
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * Remi reported this case on 10 April 2017:
	 * 
	 * MUFTUP FEDs 1384 and 1385 did not send a single event, which caused the RU to give backpressure to FED 1380
	 * 
	 * <a href= "http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-04-10-09:21:11"> daqview</a>
	 * 
	 * FIXME: problem: fmm masked
	 */
	@Test
	@Ignore
	public void remiscaseToConfirm() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491808871598.smile");

		assertSatisfiedLogicModules(snapshot, ruStuck);

	}

	/**
	 * 
	 * * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it
	 * with new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05- 18-20:15:59
	 */
	@Test
	@Ignore
	public void case1Test() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1495131324630.smile");

		assertSatisfiedLogicModules(snapshot, ruStuck);

	}

	/**
	 * * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it
	 * with new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * TODO: make sure this is related to ruStuck
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	@Ignore
	public void devcase1Test() throws URISyntaxException {
		// GMT: Sat, 26 Nov 2016 06:21:35 GMT
		DAQ snapshot = getSnapshot("1480141295312.smile");

		// TODO: why FC1?

		assertOnlyOneIsSatisified(ruStuck, snapshot);

		ContextHandler context = ruStuck.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(773)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("DT")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("DT+")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet<>(Arrays.asList("DT")), context.getContext().get("FROZENSUBSYSTEM"));

	}

	@Test
	public void test() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1531899490058.json.gz");

		assertSatisfiedLogicModules(snapshot, ruStuck, backpressureFromFerol);

		System.out.println(ruStuck.getContextHandler().getContext().getContextEntryMap());
		ContextHandler.highlightMarkup=false;
		assertEquals("RU ru-c2e13-25-01.cms is stuck. RU has more than 0 requests - 36. This causes backpressure at FED 1117 in partition HBHEC of HCAL",ruStuck.getDescriptionWithContext());
		assertThat(ruStuck.getActionWithContext(),contains("Recovery suggestion not available for this problem, contact DAQ on-call to investigate"));

	}


}
