package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class FEDDisconnectedTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1493157340132.smile");

		assertOnlyOneIsSatisified(fedDisconnected, snapshot);

	}

}
