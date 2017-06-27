package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class PiDisconnectedTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1490969849528.smile");

		System.out.println(piDisconnected.getDescriptionWithContext());

		assertOnlyOneIsSatisified(piDisconnected, snapshot);

	}

}
