package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.net.URISyntaxException;

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
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491808871598.smile");

		assertOnlyOneIsSatisified(ruStuck, snapshot);

	}

}
