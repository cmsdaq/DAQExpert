package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.UnidentifiedFailure;

import java.net.URISyntaxException;

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
        Assert.assertEquals("DAQ backpressure coming from Filter Farm. EVM has few (<strong>0</strong> requests, the threshold is <100) requests. Large fraction (<strong>73.6%</strong>, the threshold is >30%) of BUs not enabled",backpressureFromHlt.getDescriptionWithContext());
    }



    @Test
    public void shouldNotFireTest01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1480508609145.json.gz");
        Logger.getLogger(BackpressureFromHlt.class).setLevel(Level.INFO);
        assertSatisfiedLogicModules(snapshot, unidentified);
    }
}
