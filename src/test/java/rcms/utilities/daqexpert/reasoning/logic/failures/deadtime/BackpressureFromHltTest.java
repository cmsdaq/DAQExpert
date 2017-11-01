package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;

public class BackpressureFromHltTest extends FlowchartCaseTestBase {

    @Test
    public void test01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1506882001110.json.gz");
        Logger.getLogger(BackpressureFromHlt.class).setLevel(Level.INFO);
        assertOnlyOneIsSatisified(backpressureFromHlt, snapshot);
    }
}
