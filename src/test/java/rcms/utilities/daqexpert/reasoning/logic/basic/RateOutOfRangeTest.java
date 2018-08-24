package rcms.utilities.daqexpert.reasoning.logic.basic;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.ResultSupplier;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class RateOutOfRangeTest {
    @Test
    public void satisfied() throws Exception {

        RateOutOfRange rateOutOfRange = new RateOutOfRange();
        Properties p = new Properties();
        p.setProperty(Setting.EXPERT_L1_RATE_MIN.getKey(), "50000");
        p.setProperty(Setting.EXPERT_L1_RATE_MAX.getKey(), "100000");
        rateOutOfRange.parametrize(p);

        DAQ snapshot = generateSnapshot(45100);


        ResultSupplier resultSupplier = new ResultSupplier();
        resultSupplier.update(LogicModuleRegistry.StableBeams, new Output(true));
        resultSupplier.update(LogicModuleRegistry.NoRate, new Output(false));

        rateOutOfRange.setResultSupplier(resultSupplier);

        Assert.assertTrue(rateOutOfRange.satisfied(snapshot));

        ContextHandler.highlightMarkup = false;
        Assert.assertEquals("L1 rate 45.1kHz is out of expected range (50.0 - 100.0 kHz)", rateOutOfRange.getDescriptionWithContext());
    }

    @Test
    public void notWhenNoRate() throws Exception {

        RateOutOfRange rateOutOfRange = new RateOutOfRange();
        Properties p = new Properties();
        p.setProperty(Setting.EXPERT_L1_RATE_MIN.getKey(), "50000");
        p.setProperty(Setting.EXPERT_L1_RATE_MAX.getKey(), "100000");
        rateOutOfRange.parametrize(p);

        DAQ snapshot = generateSnapshot(0);


        ResultSupplier resultSupplier = new ResultSupplier();
        resultSupplier.update(LogicModuleRegistry.StableBeams, new Output(true));
        resultSupplier.update(LogicModuleRegistry.NoRate, new Output(true));

        rateOutOfRange.setResultSupplier(resultSupplier);

        Assert.assertFalse(rateOutOfRange.satisfied(snapshot));
    }


    private DAQ generateSnapshot(float rate){
        DAQ daq = new DAQ();
        FEDBuilderSummary fbs = new FEDBuilderSummary();
        daq.setFedBuilderSummary(fbs);

        fbs.setRate(rate);
        daq.setLhcBeamMode(LHCBeamMode.STABLE_BEAMS.getCode());
        return daq;

    }
}