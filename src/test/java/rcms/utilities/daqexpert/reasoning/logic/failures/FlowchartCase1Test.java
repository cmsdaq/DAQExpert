package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OutOfSequenceData;

/**
 *
 * @author holzner
 * @author mgl
 * 
 *
 */
public class FlowchartCase1Test extends FlowchartCaseTestBase {


	/**
	 * test parsing of the FED number from the RU error message for a few cases.
	 */
	@Test
	@Ignore // now covered by OutOfSequenceData LM
	public void testFEDparsing() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1493263275021.smile");

		assertSatisfiedLogicModules ( snapshot,legacyFc1);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		ContextHandler context = legacyFc1.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("622")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals("ECAL",context.getActionKey());
		assertEquals(4,legacyFc1.getActionWithContext().size());

	}

	/**
	 * 
	 * another case with a different error message
	 * 
	 */
	@Test
	public void testFEDparsing2() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480809948643.smile");

		assertSatisfiedLogicModules ( snapshot,legacyFc1);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		ContextHandler context = legacyFc1.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("548")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ES")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("ES+")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals("ES",context.getActionKey());
		assertEquals(4,legacyFc1.getActionWithContext().size());

	}

	@Test
	public void case03Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497898122474.smile");

		assertSatisfiedLogicModules ( snapshot,legacyFc1);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		ContextHandler context = legacyFc1.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("582")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("CTPPS_TOT")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TOTDET")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals("CTPPS_TOT",context.getActionKey());
		assertEquals(4,legacyFc1.getActionWithContext().size());

	}

	@Test
	public void case04Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1496315027862.smile");

		assertSatisfiedLogicModules ( snapshot,legacyFc1);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		ContextHandler context = legacyFc1.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("1326")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("FPIXM")), context.getContext().get("PROBLEM-PARTITION"));
		
		assertEquals("PIXEL",context.getActionKey());
		assertEquals(4,legacyFc1.getActionWithContext().size());

	}

	@Test
	public void case05Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1495916283277.smile");

		assertSatisfiedLogicModules ( snapshot,legacyFc1);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		ContextHandler context = legacyFc1.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("1241")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("PROBLEM-PARTITION"));
		
		assertEquals("PIXEL",context.getActionKey());
		assertEquals(4,legacyFc1.getActionWithContext().size());

	}
	
	/** testcase to check proper filling of {{PROBLEM-FED}}, {{PROBLEM-PARTITION}}
	 * and {{PROBLEM-SUBSYSTEM}} (see issue #90)
	 *
	 */
	@Test
	public void testProblemFedFilling01() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1499843690396.json.gz");

		/* Fake that OutOfSequenceData LM did not found the problem... */
		results.put(OutOfSequenceData.class.getSimpleName(), new Output(false));
		
		/* ... to give a chance to legacy LM*/
		assertEqualsAndUpdateResults(true, legacyFc1, snapshot);

		System.out.println(legacyFc1.getDescriptionWithContext());
		System.out.println(legacyFc1.getActionWithContext());

		ContextHandler context = legacyFc1.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("1111")), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("HCAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("HBHEB")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals("FED1111or1109",legacyFc1.getContextHandler().getActionKey());
		assertEquals(3,legacyFc1.getActionWithContext().size());

  }

}
