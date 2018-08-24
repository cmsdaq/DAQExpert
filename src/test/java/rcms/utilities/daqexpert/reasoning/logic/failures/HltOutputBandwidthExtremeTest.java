/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.ResultSupplier;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.basic.RunOngoing;
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
        Logger.getLogger(ContextHandler.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");

        // put holdoff periods equivalent to 'no holdoff' to mimick previous behaviour
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_RUNONGOING_HOLDOFF_PERIOD.getKey(), "0");
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_SELF_HOLDOFF_PERIOD.getKey(), "0");


        ResultSupplier resultSupplier = new ResultSupplier();
        resultSupplier.update(LogicModuleRegistry.StableBeams, new Output(true));
        resultSupplier.update(LogicModuleRegistry.HltOutputBandwidthTooHigh, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BackpressureFromHlt, new Output(false));
        resultSupplier.update(LogicModuleRegistry.RunOngoing, new Output(true));


        KnownFailure hltOutputBandwidthExtreme = new HltOutputBandwidthExtreme();
        ((Parameterizable) hltOutputBandwidthExtreme).parametrize(properties);
        hltOutputBandwidthExtreme.setResultSupplier(resultSupplier);

        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1509050129571.json");
        Assert.assertTrue(hltOutputBandwidthExtreme.satisfied(snapshot));
        Assert.assertEquals("The HLT output bandwidth is <strong>25.5GB/s</strong> " +
                "which is above the expected maximum 6.0 GB/s. " +
                "You should not continue running in these conditions. " +
                "Otherwise you risk problems with the NFS mounts on the FUs which can take a long time to recover. ",
                hltOutputBandwidthExtreme.getDescriptionWithContext());
    }

    @Ignore // no longer using notes to indicate that other problem is active. Will now use causality graph (affected nodes will be displayed)
    @Test
    public void testWithAdditionalNote() throws URISyntaxException {
        Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
        Logger.getLogger(ContextHandler.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");

        // put holdoff periods equivalent to 'no holdoff' to mimick previous behaviour
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_RUNONGOING_HOLDOFF_PERIOD.getKey(), "0");
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_SELF_HOLDOFF_PERIOD.getKey(), "0");



        ResultSupplier resultSupplier = new ResultSupplier();
        resultSupplier.update(LogicModuleRegistry.StableBeams, new Output(true));
        resultSupplier.update(LogicModuleRegistry.HltOutputBandwidthTooHigh, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BackpressureFromHlt, new Output(true));
        resultSupplier.update(LogicModuleRegistry.RunOngoing, new Output(true));

        KnownFailure hltOutputBandwidthExtreme = new HltOutputBandwidthExtreme();
        hltOutputBandwidthExtreme.setResultSupplier(resultSupplier);
        ((Parameterizable) hltOutputBandwidthExtreme).parametrize(properties);


        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1509050129571.json");
        Assert.assertTrue(hltOutputBandwidthExtreme.satisfied(snapshot));
        Assert.assertEquals("The HLT output bandwidth is <strong>25.5GB/s</strong> " +
                "which is above the expected maximum 6.0 GB/s. " +
                "You should not continue running in these conditions." +
                " Otherwise you risk problems with the NFS mounts on the FUs which can take a long time to recover. " +
                "<strong>Note that there is also backpressure from HLT.</strong>",
                hltOutputBandwidthExtreme.getDescriptionWithContext());
    }

    @Test
    public void doNotSupressExtremeTest() throws URISyntaxException {
        Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(), "4.5");

        // holdoff parameters as before introducing holdoff 2018-08
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_RUNONGOING_HOLDOFF_PERIOD.getKey(), "0");
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_SELF_HOLDOFF_PERIOD.getKey(), "0");



        ResultSupplier resultSupplier = new ResultSupplier();
        resultSupplier.update(LogicModuleRegistry.StableBeams, new Output(true));
        resultSupplier.update(LogicModuleRegistry.HltOutputBandwidthTooHigh, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BackpressureFromHlt, new Output(false));
        resultSupplier.update(LogicModuleRegistry.RunOngoing, new Output(true));


        KnownFailure hltOutputBandwidthExtreme = new HltOutputBandwidthExtreme();
        ((Parameterizable) hltOutputBandwidthExtreme).parametrize(properties);
        hltOutputBandwidthExtreme.setResultSupplier(resultSupplier);

        KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
        ((Parameterizable) hltOutputBandwidthTooHigh).parametrize(properties);
        hltOutputBandwidthTooHigh.setResultSupplier(resultSupplier);


        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1509050129571.json");
        Assert.assertTrue(hltOutputBandwidthExtreme.satisfied(snapshot));
        Assert.assertEquals("The HLT output bandwidth is <strong>25.5GB/s</strong> " +
                "which is above the expected maximum 6.0 GB/s. " +
                "You should not continue running in these conditions. " +
                "Otherwise you risk problems with the NFS mounts on the FUs which can take a long time to recover. ",
                hltOutputBandwidthExtreme.getDescriptionWithContext());

        Assert.assertTrue(hltOutputBandwidthTooHigh.satisfied(snapshot));


    }

}
