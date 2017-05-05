package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Context;

/**
 *
 * @author holzner
 */
public class FlowchartCase1Test extends FlowchartCaseTestBase {

	/**
	 * Test of satisfied method, of class FlowchartCase1.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testSatisfied() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480809948643.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		assertEqualsAndUpdateResults(false, fc2, snapshot);
		assertEqualsAndUpdateResults(false, fc3, snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, piProblem, snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected, snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem, snapshot);

		assertEqualsAndUpdateResults(false, fc5, snapshot);
		assertEqualsAndUpdateResults(false, fc6, snapshot);

		assertEqualsAndUpdateResults(false, unidentified, snapshot);

	}

	/** test parsing of the FED number from the RU error message
	 *  for a few cases.
	 */
	@Test
	public void testFEDparsing() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493263275021.smile");

		assertEqualsAndUpdateResults(true, fc1, snapshot);
		
		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("622")),  context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")),  context.getContext().get("TTCP"));
		
		
		//-----
		// another case with a different error message
		//-----
		
		
	}
	
	@Test
	public void testFEDparsing2() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480809948643.smile");
		
		assertEqualsAndUpdateResults(true, fc1, snapshot);

		Context context = fc1.getContext();
		assertEquals(new HashSet(Arrays.asList("548")), context.getContext().get("FED"));
		assertEquals(new HashSet(Arrays.asList("ES")),  context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("ES+")), context.getContext().get("TTCP"));
		
		
	}
}
