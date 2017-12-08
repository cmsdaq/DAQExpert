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
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.processing.context.SimpleContextEntry;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class RuStuckWaitingOtherTest extends FlowchartCaseTestBase {

	/**
	 * Approved message: A FED stopped sending data in subsystem PIXEL. Therefore, FED 1122 is backpressured, which
	 * causes partition HF of subsystem HF to be in WARNING TTS state. There is NOTHING wrong with HF.
	 * 
	 * andre reported 1498067573275 but backpressure appears 3 sec later in 1498067575777
	 */
	@Test
	public void backpressureTest() throws URISyntaxException {

		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.INFO);
		DAQ snapshot = getSnapshot("1498067575777.smile");

		// FIXME: why fc5?
		assertSatisfiedLogicModules(snapshot, ruStuckWaitingOther);
		System.out.println(ruStuckWaitingOther.getActionWithContext());

		ContextHandler context = ruStuckWaitingOther.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(1122)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("HF")), context.getContext().get("AFFECTED-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("HF")), context.getContext().get("AFFECTED-TTCP"));
		assertEquals(new HashSet(Arrays.asList("HFb")), context.getContext().get("AFFECTED-FED-BUILDER"));
		assertEquals(new HashSet<>(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));

		assertEquals(
				108,
				((SimpleContextEntry)context.getContext().getContextEntryMap().get("PROBLEM-FED")).getObjectSet().size());
		assertEquals(new HashSet(Arrays.asList(1122)), context.getContext().get("AFFECTED-FED"));

	}

	
	/**
	 * TODO: make sure this is related to ruStuck
	 */
	@Test
	public void devcase3Test() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.INFO);

		DAQ snapshot = getSnapshot("1498067296082.smile");

		assertSatisfiedLogicModules(snapshot, ruStuckWaitingOther);
		ContextHandler context = ruStuck.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(1386)), context.getContext().get("AFFECTED-FED"));
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("AFFECTED-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("MUTFUP")), context.getContext().get("AFFECTED-TTCP"));
		assertEquals(new HashSet<>(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));

	}
	
	
	/**
	 * TODO: make sure this is related to ruStuck
	 * 
	 * @throws URISyntaxException
	 */
	/**
	 * test to ensure that the subsystem which stopped sending fragments is correctly reported.
	 */
	@Test
	public void devcase2Test() throws URISyntaxException {

		// CEST: Fri, 9 Jun 2017 16:16:56 CEST
		// UTC: Fri, 9 Jun 2017 14:16:56 UTC
		DAQ snapshot = getSnapshot("1497017816236.smile");

		assertSatisfiedLogicModules(snapshot, ruStuckWaitingOther);

		// check the subsystem reported as being at the origin of the problem
		// (whose FEDs stopped sending data)
		ContextHandler context = ruStuck.getContextHandler();

		assertEquals(new HashSet(Arrays.asList(1404)), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("AFFECTED-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("GTUP")), context.getContext().get("AFFECTED-TTCP"));
		assertEquals(new HashSet<>(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		
		System.out.println(ruStuckWaitingOther.getDescriptionWithContext());

	}

	/**
	 * Marco reported this test case
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 */
	@Test
	@Ignore
	public void marcoCaseMissingData() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494358355135.smile");

		assertSatisfiedLogicModules(snapshot, ruStuckWaitingOther);
	}

	/**
	 * Remi reported:
	 * 
	 * HF is perfectly fine. CT-PPS FEDs 1462 and 1463 have not send any data. Therefore, HF gets back pressured by the
	 * EvB as no events can be built.
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 */
	@Test
	@Ignore
	public void remisCaseHfFine() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494331020831.smile");

		assertOnlyOneIsSatisified(ruStuckWaitingOther, snapshot);

	}

	/**
	 * Remi reported:
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05- 09-21:20:00 1494357600307
	 * 
	 * * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it
	 * with new BackpressureAnalyzer - no TTS of individual FED
	 */
	@Test
	@Ignore
	public void remisCase2() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494357600307.smile");

		assertOnlyOneIsSatisified(ruStuckWaitingOther, snapshot);
		
		System.out.println(ruStuckWaitingOther.getDescriptionWithContext());

	}
	
	@Test
	public void dontWaitForPseudoFedsTest() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1504266974356.json.gz");

		assertSatisfiedLogicModules(snapshot,ruStuckWaitingOther, ruFailed);

	}
}
