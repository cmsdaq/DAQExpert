package rcms.utilities.daqexpert.reasoning.logic.basic;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.RateTooHigh;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class FEDDeadtimeTest {

    @Test
    public void test01() throws URISyntaxException {
        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1510239315441.json.gz");
        Map<String, Output> results = new HashMap<>();


        results.put(ExpectedRate.class.getSimpleName(), new Output(true));
        results.put(StableBeams.class.getSimpleName(), new Output(true));
        results.put(LongTransition.class.getSimpleName(), new Output(false));

        FEDDeadtime module = new FEDDeadtime();

        // mock parameters
        Properties config = new Properties();
        config.put(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(config);

        // run module to be tested
        boolean result = module.satisfied(snapshot, results);

        assertEquals(true, result);

        Assert.assertEquals("Deadtime of fed(s) <strong>582</strong> in subsystem(s) <strong>CTPPS_TOT</strong> is <strong>100%</strong> , the threshold is 2.0%",module.getDescriptionWithContext());

        System.out.println("description=" + module.getDescriptionWithContext());

    }
}
