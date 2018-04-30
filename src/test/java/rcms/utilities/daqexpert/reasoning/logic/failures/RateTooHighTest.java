/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 *
 * @author holzner
 */
public class RateTooHighTest
{

	@Test
	public void test01() throws URISyntaxException
	{
		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1503256889057.json.gz");

		Map<String, Output> results = new HashMap<>();

		results.put(StableBeams.class.getSimpleName(), new Output(true));

		// ensure that the RateTooHighTest module fires
		RateTooHigh module = new RateTooHigh();

		// mock parameters
		Properties config = new Properties();
		config.put(Setting.EXPERT_L1_RATE_MAX.getKey(), "100000");
		module.parametrize(config);

		// run module to be tested
		boolean result = module.satisfied(snapshot, results);

		assertEquals(true, result);

		// System.out.println("description=" + module.getDescriptionWithContext());

	}

}
