package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase5Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1479614378467.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);

	}

}
