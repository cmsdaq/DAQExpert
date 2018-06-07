package rcms.utilities.daqexpert.reasoning.logic.basic;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class TmpUpgradedFedProblemTest extends FlowchartCaseTestBase{
    @Test
    public void satisfiedOnUpgradedFed() throws Exception {

        DAQ daq = getSnapshot("1525857827659.json.gz");
        TmpUpgradedFedProblem lm = new TmpUpgradedFedProblem();
        Properties p = new Properties();
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        lm.parametrize(p);
        Map<String, Output> r = new HashMap<>();
        r.put(TTSDeadtime.class.getSimpleName(), new Output(true));
        Assert.assertTrue(lm.satisfied(daq, r));

    }

    @Test
    public void notSatisfiedOnLegacyFed() throws Exception {

        DAQ daq = getSnapshot("1526094980064.json.gz");
        TmpUpgradedFedProblem lm = new TmpUpgradedFedProblem();
        Properties p = new Properties();
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        lm.parametrize(p);
        Map<String, Output> r = new HashMap<>();
        r.put(TTSDeadtime.class.getSimpleName(), new Output(true));
        Assert.assertFalse(lm.satisfied(daq, r));

    }
}