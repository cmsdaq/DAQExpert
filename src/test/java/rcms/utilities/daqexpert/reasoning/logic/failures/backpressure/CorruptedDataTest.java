package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.TestBase;

/**
 * Test corrupted data discovery
 * 
 * @author Maciej Gladki
 */
public class CorruptedDataTest extends FlowchartCaseTestBase {

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-24-05:12:16
	 */
	@Test
	public void ecalSpecificCase2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498273936869.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

		ContextHandler.highlightMarkup=false;

		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().getReusableContextEntry("PROBLEM-FED").getObjectSet().stream().map(f->((FED)f).getSrcIdExpected()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().getReusableContextEntry("PROBLEM-SUBSYSTEM").getObjectSet().stream().map(f->((SubSystem)f).getName()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().getReusableContextEntry("PROBLEM-PARTITION").getObjectSet().stream().map(f->((TTCPartition)f).getName()).collect(Collectors.toSet()));

		assertEquals(4, fc2.getActionWithContext().size());

	}

	/**
	 * http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq&time=2017-03-27-15:52:23
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 * 
	 * @throws URISyntaxException
	 */
	@Ignore
	@Test
	public void ecalFedCorruptedDataTest() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1490622743834.smile");
		assertSatisfiedLogicModules(snapshot, fc2); // FC2 not identified with new BackpressureAnalyzer

//		assertEquals(1, fc2.getContextHandler().getContext().get("PROBLEM-FED").size());
//		assertEquals(644, fc2.getContextHandler().getContext().get("PROBLEM-FED").iterator().next());
//		assertEquals(1, fc2.getContextHandler().getContext().get("AFFECTED-FED").size());
//		assertEquals(1360, fc2.getContextHandler().getContext().get("AFFECTED-FED").iterator().next());

	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-05-18-20:16:51
	 * 
	 * Ignored because it's impossible to identify it with new approach - there is no partitions in B/W
	 */
	@Test
	@Ignore
	public void case4Test() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1495131411780.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2016-11-10-16:55:37
	 * 
	 * NOTE that snapshot was produced before (TTS monitoring of upgraded FEDs) - it's not possible to identify it with
	 * new BackpressureAnalyzer - no TTS of individual FED
	 */
	@Ignore
	@Test
	public void ecalSpecificCaseTest() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1478793337902.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

		// TODO: why ruFailed?

		assertEquals(false, unidentified.satisfied(snapshot, results));
		System.out.println("Output: " + fc2.getDescriptionWithContext());
		System.out.println("Output: " + fc2.getActionWithContext());

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(644)), context.getContext().get("PROBLEM-FED"));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
		assertEquals(new HashSet(Arrays.asList("EB+")), context.getContext().get("PROBLEM-PARTITION"));

		assertEquals(4, fc2.getActionWithContext().size());
	}

	@Test
	public void nonEcalTest() throws URISyntaxException {

		TestBase tester = new TestBase();

		tester.runLogic("1498274256030.smile");

		tester.assertSatisfied(LogicModuleRegistry.CorruptedData);
		tester.assertSatisfied(LogicModuleRegistry.RuFailed); //? why satisfied?

		Output output = tester.getOutputOf(LogicModuleRegistry.CorruptedData);




		assertEquals(new HashSet(Arrays.asList(833)), output.getContext().getReusableContextEntry("PROBLEM-FED").getObjectSet().stream().map(f->((FED)f).getSrcIdExpected()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("CSC")), output.getContext().getReusableContextEntry("PROBLEM-SUBSYSTEM").getObjectSet().stream().map(f->((SubSystem)f).getName()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("CSC+")), output.getContext().getReusableContextEntry("PROBLEM-PARTITION").getObjectSet().stream().map(f->((TTCPartition)f).getName()).collect(Collectors.toSet()));

		//assertEquals(3, fc2.getActionWithContext().size());
	}

	@Test
	public void case2TestNew() {

		TestBase tester = new TestBase();

		Properties properties = tester.getEmptyProperties();
		properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(),"2");
		properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(),"2");
		properties.setProperty(Setting.EXPERT_LOGIC_EVM_FEW_EVENTS.getKey(),"100");
		properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TTS.getKey(), "2");

		Map<String, Output> results = tester.runLogic("1497448564059.smile", properties);

		Assert.assertEquals(LogicModuleRegistry.CorruptedData, tester.dominating.getLogicModule());

		Context context = results.get(CorruptedData.class.getSimpleName()).getContext();
		assertEquals(new HashSet(Arrays.asList(841,843)), context.getReusableContextEntry("PROBLEM-FED").getObjectSet().stream().map(f->((FED)f).getSrcIdExpected()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getReusableContextEntry("PROBLEM-SUBSYSTEM").getObjectSet().stream().map(f->((SubSystem)f).getName()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getReusableContextEntry("PROBLEM-PARTITION").getObjectSet().stream().map(f->((TTCPartition)f).getName()).collect(Collectors.toSet()));



	}

	@Test
	public void case2Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497448564059.smile");

		// NOTE multiple LMs satisfied
		ContextHandler.highlightMarkup=false;
		assertSatisfiedLogicModules(snapshot, fc2, fc3, ruFailed);

		ContextHandler context = fc2.getContextHandler();


		assertEquals(new HashSet(Arrays.asList(841,843)), context.getContext().getReusableContextEntry("PROBLEM-FED").getObjectSet().stream().map(f->((FED)f).getSrcIdExpected()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("CSC")), context.getContext().getReusableContextEntry("PROBLEM-SUBSYSTEM").getObjectSet().stream().map(f->((SubSystem)f).getName()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("CSC+")), context.getContext().getReusableContextEntry("PROBLEM-PARTITION").getObjectSet().stream().map(f->((TTCPartition)f).getName()).collect(Collectors.toSet()));



		assertEquals(3, fc2.getActionWithContext().size());
	}

	/*
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-27-10:20:36
	 * 
	 * corrupted-data (fc2) is correct. FED stuck should not trigger because 1120 is backpressured.
	 */
	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1498551636794.smile");

		// FIXME: why ruFailed is satisfied?
		assertSatisfiedLogicModules(snapshot, fc2, ruFailed);

		ContextHandler context = fc2.getContextHandler();
		assertEquals(new HashSet(Arrays.asList(622)), context.getContext().getReusableContextEntry("PROBLEM-FED").getObjectSet().stream().map(f->((FED)f).getSrcIdExpected()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("ECAL")), context.getContext().getReusableContextEntry("PROBLEM-SUBSYSTEM").getObjectSet().stream().map(f->((SubSystem)f).getName()).collect(Collectors.toSet()));
		assertEquals(new HashSet(Arrays.asList("EB-")), context.getContext().getReusableContextEntry("PROBLEM-PARTITION").getObjectSet().stream().map(f->((TTCPartition)f).getName()).collect(Collectors.toSet()));



		assertEquals(4, fc2.getActionWithContext().size());
	}

}
