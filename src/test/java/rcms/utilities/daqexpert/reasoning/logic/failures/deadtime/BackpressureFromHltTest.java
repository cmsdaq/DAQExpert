package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BackpressureFromHltTest extends FlowchartCaseTestBase {

    @Test
    public void test01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1506882001110.json.gz");
        Logger.getLogger(BackpressureFromHlt.class).setLevel(Level.INFO);
        assertOnlyOneIsSatisified(backpressureFromHlt, snapshot);
    }

    @Test
    public void test02() throws URISyntaxException {
        Logger.getLogger(BackpressureFromHlt.class).setLevel(Level.ALL);
        DAQ snapshot = getSnapshot("1506881550007.json.gz");
        assertOnlyOneIsSatisified(backpressureFromHlt, snapshot);
        Assert.assertEquals("DAQ backpressure coming from Filter Farm. EVM has few (<strong>0</strong> requests, the threshold is <100) requests. Large fraction (<strong>73.6%</strong>, the threshold is >30%) of BUs not enabled", backpressureFromHlt.getDescriptionWithContext());
    }


    @Test
    public void shouldNotFireTest01() throws URISyntaxException {

        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        properties.setProperty(Setting.EXPERT_LOGIC_BACKPRESSUREFROMHLT_THRESHOLD_BUS.getKey(), ".3");
        properties.setProperty(Setting.EXPERT_LOGIC_EVM_FEW_EVENTS.getKey(), "100");
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");

        DAQ snapshot = getSnapshot("1480508609145.smile.gz");
        Logger.getLogger(BackpressureFromHlt.class).setLevel(Level.INFO);

        Map<String, Boolean> r = new HashMap<>();
        r.put(StableBeams.class.getSimpleName(), true);
        r.put(ExpectedRate.class.getSimpleName(),true);
        r.put(FEDDeadtime.class.getSimpleName(),true);


        FedDeadtimeDueToDaq fd = new FedDeadtimeDueToDaq();
        fd.parametrize(properties);
        Assert.assertFalse(fd.satisfied(snapshot,r));

        r.put(FedDeadtimeDueToDaq.class.getSimpleName(), false);

        BackpressureFromHlt module = new BackpressureFromHlt();
        module.parametrize(properties);
        Assert.assertFalse(module.satisfied(snapshot, r));
    }
}
