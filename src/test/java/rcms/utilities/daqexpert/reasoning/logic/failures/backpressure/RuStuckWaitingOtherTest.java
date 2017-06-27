package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.net.URISyntaxException;

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
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494358355135.smile");

		assertOnlyOneIsSatisified(ruStuckWaitingOther, snapshot);
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
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494331020831.smile");

		assertOnlyOneIsSatisified(ruStuckWaitingOther, snapshot);

	}

	/**
	 * Remi reported:
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-
	 * 09-21:20:00 1494357600307
	 */
	@Test
	public void remisCase2() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1494357600307.smile");

		assertOnlyOneIsSatisified(ruStuckWaitingOther, snapshot);

	}
}
