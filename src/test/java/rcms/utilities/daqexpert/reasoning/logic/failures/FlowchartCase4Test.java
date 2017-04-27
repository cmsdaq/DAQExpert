package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 *
 * @author Maciej Gladki
 * 
 */
@Ignore // TODO: find the case
public class FlowchartCase4Test extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1478748186297.smile");
		Map<String, Boolean> results = new HashMap<String, Boolean>();

		results.put("StableBeams", true);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));
		System.out.println("FC3 context: " + fc3.getContext().getContext());

		// new subcases of old flowchart case 4
		assertEquals(false, piProblem.satisfied(snapshot, results));
		assertEquals(false, fedDisconnected.satisfied(snapshot, results));
		assertEquals(false, fmmProblem.satisfied(snapshot, results));
		assertEquals(true, piDisconnected.satisfied(snapshot, results));
		System.out.println("FC4 context: " + piDisconnected.getContext().getContext());

		assertEquals(false, fc5.satisfied(snapshot, results));
		System.out.println("FC5 context: " + fc5.getContext().getContext());
		assertEquals(false, fc6.satisfied(snapshot, results));

	}

}
