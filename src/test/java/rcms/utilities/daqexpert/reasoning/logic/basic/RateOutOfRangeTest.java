package rcms.utilities.daqexpert.reasoning.logic.basic;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
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

        Map<String, Output> results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), new Output(true));
        results.put(NoRate.class.getSimpleName(), new Output(false));

        Assert.assertTrue(rateOutOfRange.satisfied(snapshot, results));

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

        Map<String, Output> results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), new Output(true));
        results.put(NoRate.class.getSimpleName(), new Output(true));

        Assert.assertFalse(rateOutOfRange.satisfied(snapshot, results));
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