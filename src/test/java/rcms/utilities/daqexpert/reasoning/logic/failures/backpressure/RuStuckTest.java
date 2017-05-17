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
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491808871598.smile");
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
		assertEquals(true, ruStuck.satisfied(snapshot, results));

		assertEquals(false, ruStuckWaiting.satisfied(snapshot, results));

		System.out.println("New message:");
		System.out.println(ruStuck.getDescriptionWithContext());
	}

}
