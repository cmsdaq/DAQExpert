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
		Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.ALL);
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(),"4.0");
		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1507212269717.json");
		KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
		((Parameterizable)hltOutputBandwidthTooHigh).parametrize(properties);
		Map<String, Boolean> results = new HashMap<>();
		results.put(StableBeams.class.getSimpleName(), true);
		Assert.assertTrue(hltOutputBandwidthTooHigh.satisfied(snapshot,results));
	}

}
