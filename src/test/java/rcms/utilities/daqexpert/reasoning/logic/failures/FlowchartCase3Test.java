package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.*;

import org.junit.Test;

import rcms.fm.resource.qualifiedresource.Subsystem;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 *
 * @author Maciej Gladki
 */
public class FlowchartCase3Test extends FlowchartCaseTestBase {

	/* 2016-11-10T04:23:06 */
	@Test
	public void case1Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1478748186297.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		ContextHandler context = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().get("TTCP"));
	}

	/* 2016-12-04T02:05:40 */
	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1480813540739.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		ContextHandler context = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));
	}

	/* 2017-04-07T16:51:54 */
	@Test
	public void ttsAtTopFMMNullButPmOutOfSyncTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1491576714151.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);
		ContextHandler context = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("TRG")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("MUTFUP")), context.getContext().get("TTCP"));
	}


	/* 2017-06-05T09:22:29 */
	@Test
	public void case4Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1496647349638.smile");
		assertOnlyOneIsSatisified(fc3, snapshot);

		ContextHandler context = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("PIXEL")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("BPIXP")), context.getContext().get("TTCP"));
	}

	/*
	 * 2017-06-14T15:56:04
	 * 
	 * REMI: The message (ru-failed) is indeed redundant. However, the RU error message gives the details about the
	 * corruption. Is it possible to add the error message to the 'Corrupted data received' text?
	 * 
	 * HANNES: This is s strange case with multiple problems overlayed. It would be good to add the detailed error
	 * message of 3 also to 2. We should check if we can make RU-failed more specific so that it does not trigger in
	 * this case
	 * 
	 */
	@Test
	public void case5Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		// FIXME: ru failed redundant
		assertSatisfiedLogicModules(snapshot, fc2, fc3, ruFailed);

		ContextHandler context = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("TRACKER")), context.getContext().get("SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("TIBTID")), context.getContext().get("TTCP"));

	}

	@Test
	public void ecalEsInUnstableLHCClockSpecificCaseTest() throws URISyntaxException {
		DAQ daq = new DAQ();

		Map<String, Output> r = new HashMap<>();

		r.put(NoRateWhenExpected.class.getSimpleName(), new Output(true));
		r.put(StableBeams.class.getSimpleName(), new Output(false ));


		daq.setClockSource("LHC");
		daq.setLhcClockStable(false);
		List<SubSystem> subsystemList = new ArrayList<>();
		SubSystem ecal = new SubSystem();
		ecal.setName("ECAL");
		TTCPartition test = new TTCPartition();
		test.setName("TestPartition");
		test.setTtsState(TTSState.OUT_OF_SYNC.getCode());
		ecal.setTtcPartitions(new HashSet<>());
		ecal.getTtcPartitions().add(test);
		ecal.setStatus(TTSState.OUT_OF_SYNC.getCode());
		subsystemList.add(ecal);
		daq.setSubSystems(subsystemList);

		ContextHandler.highlightMarkup= false;
		fc3.satisfied(daq, r);

		System.out.println(fc3.getDescriptionWithContext());
		System.out.println(fc3.getActionWithContext());

		assertEquals(3, fc3.getActionWithContext().size());

		ContextHandler context = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("SUBSYSTEM"));


		daq.setLhcClockStable(true);
		fc3.satisfied(daq, r);
		assertEquals(6, fc3.getActionWithContext().size());


	}

}
