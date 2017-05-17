package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class RuStuckWaitingOtherTest extends FlowchartCaseTestBase {

	/**
	 * Marco reported this test case
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void marcoCaseMissingData() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494358355135.smile");
		Map<String, Boolean> results = new HashMap();
		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEquals(false, fc5.satisfied(snapshot, results));

		assertEquals(false, b1.satisfied(snapshot, results));
		assertEquals(false, b2.satisfied(snapshot, results));
		assertEquals(false, b3.satisfied(snapshot, results));
		assertEquals(false, b4.satisfied(snapshot, results));
		assertEquals(false, ruStuck.satisfied(snapshot, results));

		assertEquals(false, ruStuckWaiting.satisfied(snapshot, results));
		assertEquals(true, ruStuckWaitingOther.satisfied(snapshot, results));

		System.out.println("New message:");
		System.out.println(ruStuckWaitingOther.getDescriptionWithContext());
	}

	/**
	 * Remi reported:
	 * 
	 * HF is perfectly fine. CT-PPS FEDs 1462 and 1463 have not send any data.
	 * Therefore, HF gets back pressured by the EvB as no events can be built.
	 * 
	 * <a href=
	 * "http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-09-13:57:00">
	 * daqview</a>
	 * 
	 */
	@Test
	public void remisCaseHfFine() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494331020831.smile");
		Map<String, Boolean> results = new HashMap();
		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEquals(false, fc5.satisfied(snapshot, results));

		assertEquals(false, b1.satisfied(snapshot, results));
		assertEquals(false, b2.satisfied(snapshot, results));
		assertEquals(false, b3.satisfied(snapshot, results));
		assertEquals(false, b4.satisfied(snapshot, results));
		assertEquals(false, ruStuck.satisfied(snapshot, results));

		assertEquals(false, ruStuckWaiting.satisfied(snapshot, results));
		assertEquals(true, ruStuckWaitingOther.satisfied(snapshot, results));

		System.out.println("New message:");
		System.out.println(ruStuckWaitingOther.getDescriptionWithContext());
	}

	/**
	 * Remi reported:
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-
	 * 09-21:20:00 1494357600307
	 */
	@Test
	public void remisCase2() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494357600307.smile");
		Map<String, Boolean> results = new HashMap();
		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEquals(false, fc5.satisfied(snapshot, results));

		assertEquals(false, b1.satisfied(snapshot, results));
		assertEquals(false, b2.satisfied(snapshot, results));
		assertEquals(false, b3.satisfied(snapshot, results));
		assertEquals(false, b4.satisfied(snapshot, results));
		assertEquals(false, ruStuck.satisfied(snapshot, results));

		assertEquals(false, ruStuckWaiting.satisfied(snapshot, results));
		assertEquals(true, ruStuckWaitingOther.satisfied(snapshot, results));

		System.out.println("New message:");
		System.out.println(ruStuckWaitingOther.getDescriptionWithContext());
	}
}
