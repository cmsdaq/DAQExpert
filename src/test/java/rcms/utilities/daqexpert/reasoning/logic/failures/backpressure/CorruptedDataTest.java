package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

/**
 * Test corrupted data discovery
 * 
 * @author Maciej Gladki
 * 
 *         TODO: investigate 1478793337902
 */
public class CorruptedDataTest extends FlowchartCaseTestBase {

	/**
	 * http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2017-
	 * 03-27-15:52:23
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void ecalFedCorruptedDataTest() throws URISyntaxException {
		// Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1490622743834.smile");

		assertOnlyOneIsSatisified(fc2, snapshot);

		assertEquals(1, fc2.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(644, fc2.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc2.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1360, fc2.getContext().getContext().get("AFFECTED-FED").iterator().next());

	}

	/**
	 * Test case taken from legacy test FC2
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1478793337902.smile");

		assertOnlyOneIsSatisified(fc2, snapshot);

	}

}
