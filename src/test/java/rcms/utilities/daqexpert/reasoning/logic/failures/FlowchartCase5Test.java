package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.jobs.RecoveryRequestBuilder;
import rcms.utilities.daqexpert.jobs.RecoveryRequest;
import rcms.utilities.daqexpert.jobs.RecoveryStep;
import rcms.utilities.daqexpert.processing.context.ContextHandler;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase5Test extends FlowchartCaseTestBase {

	/* 2016-11-20T04:59:38 */
	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1479614378467.smile");
		ContextHandler.highlightMarkup =false;
		assertOnlyOneIsSatisified(fc5, snapshot);
		ContextHandler context = fc5.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TEC-")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet(Arrays.asList(169)), context.getContext().get("PROBLEM-FED"));
		

		assertEquals(new HashSet(Arrays.asList("WARNING")), context.getContext().get("TTCPSTATE"));

		assertEquals("TRACKER", context.getActionKey());
		assertEquals(4,fc5.getActionWithContext().size());

		assertEquals(Arrays.asList("Issue a TTCResync once",
				"Problem fixed: Make an e-log entry." ,
				"Problem not fixed: Stop the run, red recycle TRACKER, start a new run","Problem still not fixed: Call the DOC for the TRACKER"), fc5.getActionWithContext());

		RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
		RecoveryRequest recoveryRequests = recoveryRequestBuilder.buildRecoveryRequest(fc5.getActionWithContextRawRecovery(), fc5.getName(), fc5.getDescriptionWithContext(), 0L);
		assertEquals(0, recoveryRequests.getRecoverySteps().size());

	}

	/* http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-26-03:28:25 */
	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498440505470.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);
		ContextHandler context = fc5.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet(Arrays.asList(149)), context.getContext().get("PROBLEM-FED"));
		

		assertEquals(new HashSet(Arrays.asList("WARNING")), context.getContext().get("TTCPSTATE"));

		assertEquals("TRACKER", context.getActionKey());
		assertEquals(4,fc5.getActionWithContext().size());

		assertEquals(Arrays.asList("Issue a TTCResync once", "Problem fixed: Make an e-log entry.",
				"Problem not fixed: Stop the run, red recycle TRACKER, start a new run",  "Problem still not fixed: Call the DOC for the TRACKER"), fc5.getActionWithContext());

		RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
		RecoveryRequest recoveryRequest = recoveryRequestBuilder.buildRecoveryRequest(fc5.getActionWithContextRawRecovery(),fc5.getName(), fc5.getDescriptionWithContext(), 0L);
		assertEquals(0, recoveryRequest.getRecoverySteps().size());
	}

	@Test
	public void multipleFedsProblemTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1527446460841.json.gz");

		assertSatisfiedLogicModules(snapshot, fc5, fc3);


		ContextHandler context = fc5.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("GEM", "TRG")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("GEMPILOT1", "MUTF")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet(Arrays.asList(1467, 1380, 1381)), context.getContext().get("PROBLEM-FED"));

		ContextHandler.highlightMarkup = false;
		assertEquals("TTCP [GEMPILOT1, MUTF] of [GEM, TRG] subsystem is blocking triggers, it's in BUSY TTS state, The problem is caused by FED [1380-1381, 1467] in BUSY", fc5.getDescriptionWithContext());


	}

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-22-04:01:25
	 * 
	 * REMI: this issue has been discussed in detail in the email thread
	 * "Fwd: ELOG : DAQ : Dump of FEROL40 with FED Id [1232 and 6 more] when blocking the run" on June 22/23. I think
	 * the conclusion is that we do not know if the FEROL40 was indeed stuck for a couple of seconds, or if there was a
	 * monitoring hiccup or anything else. I would keep the message as is for now and see if we can find another case.
	 *
	 */
	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498096885568.smile");

		// FIXME: we dont know why ferol fifo stuck here: we keep it though?
		ContextHandler.highlightMarkup = false;
		assertSatisfiedLogicModules(snapshot, fc5, ferolFifoStuck);

		System.out.println(fc5.getDescriptionWithContext());

		ContextHandler context = fc5.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet(Arrays.asList(838)), context.getContext().get("PROBLEM-FED"));
		

		assertEquals("CSC",context.getActionKey());
		assertEquals(3,fc5.getActionWithContext().size());

		assertEquals(Arrays.asList("Stop and start the run with Red recycle of subsystem CSC and Green recycle of subsystem CSC (try up to 2 times)" ,
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem CSC to inform",
				"Problem not fixed: Call the DOC for the subsystem CSC"), fc5.getActionWithContext());

		RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
		RecoveryRequest recoveryRequests = recoveryRequestBuilder.buildRecoveryRequest(fc5.getActionWithContextRawRecovery(),fc5.getName(), fc5.getDescriptionWithContext(), 0L);
		assertEquals(1, recoveryRequests.getRecoverySteps().size());
		RecoveryStep recoveryStep = recoveryRequests.getRecoverySteps().iterator().next();
		assertEquals(1, recoveryStep.getRedRecycle().size());
		assertEquals(1, recoveryStep.getGreenRecycle().size());
		assertEquals("CSC", recoveryStep.getRedRecycle().iterator().next());
		assertEquals("CSC", recoveryStep.getGreenRecycle().iterator().next());

	}

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-15-23:29:34
	 */
	@Test
	public void case5Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497562174081.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);

		ContextHandler.highlightMarkup = false;
		System.out.println(fc5.getDescriptionWithContext());

		ContextHandler context = fc5.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("HCAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("HBHEC")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet(Arrays.asList(11114)), context.getContext().get("PROBLEM-FED"));
		

		assertEquals("HCAL",context.getActionKey());
		assertEquals(3,fc5.getActionWithContext().size());

		assertEquals(Arrays.asList(
				"Stop and start the run with Red recycle of subsystem HCAL and Green recycle of subsystem HCAL (try up to 2 times)" ,
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem HCAL to inform","Problem not fixed: Call the DOC for the subsystem HCAL"), fc5.getActionWithContext());

		RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
		RecoveryRequest recoveryRequests = recoveryRequestBuilder.buildRecoveryRequest(fc5.getActionWithContextRawRecovery(),fc5.getName(), fc5.getDescriptionWithContext(), 0L);
		assertEquals(1, recoveryRequests.getRecoverySteps().size());
		RecoveryStep recoveryStep = recoveryRequests.getRecoverySteps().iterator().next();
		assertEquals(1, recoveryStep.getRedRecycle().size());
		assertEquals(1, recoveryStep.getGreenRecycle().size());
		assertEquals("HCAL", recoveryStep.getRedRecycle().iterator().next());
		assertEquals("HCAL", recoveryStep.getGreenRecycle().iterator().next());
	}

	/* http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-15-09:52:16 */
	@Test
	public void case4Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497513136376.smile");

		assertOnlyOneIsSatisified(fc5, snapshot);
		ContextHandler context = fc5.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("PROBLEM-PARTITION"));
		assertEquals(new HashSet(Arrays.asList(83)), context.getContext().get("PROBLEM-FED"));
		

		assertEquals(new HashSet(Arrays.asList("WARNING")), context.getContext().get("TTCPSTATE"));

		assertEquals("TRACKER", context.getActionKey());
		assertEquals(4,fc5.getActionWithContext().size());

		assertEquals(Arrays.asList("Issue a TTCResync once",
				"Problem fixed: Make an e-log entry." ,
				"Problem not fixed: Stop the run, red recycle TRACKER, start a new run","Problem still not fixed: Call the DOC for the TRACKER"), fc5.getActionWithContext());

		RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
		RecoveryRequest recoveryRequests = recoveryRequestBuilder.buildRecoveryRequest(fc5.getActionWithContextRawRecovery(),fc5.getName(), fc5.getDescriptionWithContext(), 0L);
		assertEquals(0, recoveryRequests.getRecoverySteps().size());
	}

}
