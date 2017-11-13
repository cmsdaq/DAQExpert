/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author mgl
 */
public class HltOutputBandwidthTooHighTest
{

	@Test
	public void test01() throws URISyntaxException
	{
		Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(),"4.5");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(),"6.0");
		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1507212269717.json");
		KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
		((Parameterizable)hltOutputBandwidthTooHigh).parametrize(properties);
		Map<String, Boolean> results = new HashMap<>();
		results.put(StableBeams.class.getSimpleName(), true);
		results.put(BackpressureFromHlt.class.getSimpleName(), false);
		Assert.assertTrue(hltOutputBandwidthTooHigh.satisfied(snapshot,results));
		Assert.assertEquals("The HLT output bandwidth is <strong>4.7GB/s</strong> which is above the threshold of 4.5 GB/s at which delays Rate Monitoring and Express streams can appear. DQM files may get truncated resulting in lower statistics. ",hltOutputBandwidthTooHigh.getDescriptionWithContext());
	}

	@Test
	public void testWithAdditionalNote() throws URISyntaxException
	{
		Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(),"4.5");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(),"6.0");
		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1507212269717.json");
		KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
		((Parameterizable)hltOutputBandwidthTooHigh).parametrize(properties);
		Map<String, Boolean> results = new HashMap<>();
		results.put(StableBeams.class.getSimpleName(), true);
		results.put(BackpressureFromHlt.class.getSimpleName(), true);
		Assert.assertTrue(hltOutputBandwidthTooHigh.satisfied(snapshot,results));
		Assert.assertEquals("The HLT output bandwidth is <strong>4.7GB/s</strong> which is above the threshold of 4.5 GB/s at which delays Rate Monitoring and Express streams can appear. DQM files may get truncated resulting in lower statistics. <strong>Note that there is also backpressure from HLT.</strong>",hltOutputBandwidthTooHigh.getDescriptionWithContext());
	}

}
