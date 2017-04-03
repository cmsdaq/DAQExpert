package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

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
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1490622743834.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(true, fc2.satisfied(snapshot, results));
		System.out.println(fc2.getDescriptionWithContext());
		assertEquals(1, fc2.getContext().getContext().get("PROBLEM-FED").size());
		assertEquals(644, fc2.getContext().getContext().get("PROBLEM-FED").iterator().next());
		assertEquals(1, fc2.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(1360, fc2.getContext().getContext().get("AFFECTED-FED").iterator().next());

		assertEquals(false, fc3.satisfied(snapshot, results));
		assertEquals(false, fc4.satisfied(snapshot, results));
		assertEquals(false, fc5.satisfied(snapshot, results));
		// assertEquals(false, fc6.satisfied(snapshot, results));

	}

}
