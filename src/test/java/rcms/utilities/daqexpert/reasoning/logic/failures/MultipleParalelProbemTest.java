package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.processing.context.SimpleContextEntry;

public class MultipleParalelProbemTest extends FlowchartCaseTestBase {
	/*
	 * 2017-06-20T14:56:45
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-20-14:56:45
	 * 
	 * Confirmed in https://github.com/cmsdaq/DAQExpert/issues/88
	 */
	@Test
	public void case3Test() throws URISyntaxException {
		DAQ snapshot = getSnapshot("1497963405145.smile");

		assertSatisfiedLogicModules(snapshot, fc3, legacyFc1);

		ContextHandler contextHandler = fc3.getContextHandler();
		assertEquals(new HashSet(Arrays.asList("CTPPS_TOT")), ((SimpleContextEntry<String>)contextHandler.getContext().getContextEntryMap().get("SUBSYSTEM")).getObjectSet());
		assertEquals(new HashSet(Arrays.asList("TOTDET")), ((SimpleContextEntry<String>)contextHandler.getContext().getContextEntryMap().get("TTCP")).getObjectSet());

	}

	/**
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-17-14:44:51
	 * 
	 * Confirmed in https://github.com/cmsdaq/DAQExpert/issues/88
	 */
	@Test
	public void test06() throws URISyntaxException {
		// Sun, 17 Jun 2017 14:44:52 CEST
		// Sun, 17 Jun 2017 12:44:520 UTC

		DAQ snapshot = getSnapshot("1497703492467.smile");
		assertSatisfiedLogicModules(snapshot, legacyFc1, fc5);

	}
}
