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
import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author mgl
 */
public class HltOutputBandwidthExtremeTest {

    @Test
    public void test01() throws URISyntaxException {
        Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
        Logger.getLogger(Context.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");
        Map<String, Boolean> results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), true);
        results.put(BackpressureFromHlt.class.getSimpleName(), false);

        KnownFailure hltOutputBandwidthExtreme = new HltOutputBandwidthExtreme();
        ((Parameterizable) hltOutputBandwidthExtreme).parametrize(properties);


        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1509050129571.json");
        Assert.assertTrue(hltOutputBandwidthExtreme.satisfied(snapshot, results));
        Assert.assertEquals("The HLT output bandwidth is <strong>25.5GB/s</strong> " +
                "which is above the expected maximum 6.0 GB/s. ", hltOutputBandwidthExtreme.getDescriptionWithContext());
    }

    @Test
    public void testWithAdditionalNote() throws URISyntaxException {
        Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
        Logger.getLogger(Context.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");
        Map<String, Boolean> results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), true);
        results.put(BackpressureFromHlt.class.getSimpleName(), true);

        KnownFailure hltOutputBandwidthExtreme = new HltOutputBandwidthExtreme();
        ((Parameterizable) hltOutputBandwidthExtreme).parametrize(properties);


        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1509050129571.json");
        Assert.assertTrue(hltOutputBandwidthExtreme.satisfied(snapshot, results));
        Assert.assertEquals("The HLT output bandwidth is <strong>25.5GB/s</strong> " +
                "which is above the expected maximum 6.0 GB/s. <strong>Note that there is also backpressure from HLT.</strong>", hltOutputBandwidthExtreme.getDescriptionWithContext());
    }

    @Test
    public void extremeSupressHighTest() throws URISyntaxException {
        Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(), "4.5");
        Map<String, Boolean> results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), true);
        results.put(BackpressureFromHlt.class.getSimpleName(), false);


        KnownFailure hltOutputBandwidthExtreme = new HltOutputBandwidthExtreme();
        ((Parameterizable) hltOutputBandwidthExtreme).parametrize(properties);

        KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
        ((Parameterizable) hltOutputBandwidthTooHigh).parametrize(properties);


        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1509050129571.json");
        Assert.assertTrue(hltOutputBandwidthExtreme.satisfied(snapshot, results));
        Assert.assertEquals("The HLT output bandwidth is <strong>25.5GB/s</strong> " +
                "which is above the expected maximum 6.0 GB/s. ", hltOutputBandwidthExtreme.getDescriptionWithContext());

        Assert.assertFalse(hltOutputBandwidthTooHigh.satisfied(snapshot, results));


    }

}
