package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;

public class BackpressureFromFerolTest extends FlowchartCaseTestBase {

    @Test
    public void test01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1507212240143.json.gz");
        Logger.getLogger(BackpressureFromFerol.class).setLevel(Level.INFO);
        assertOnlyOneIsSatisified(backpressureFromFerol, snapshot);
    }
}
