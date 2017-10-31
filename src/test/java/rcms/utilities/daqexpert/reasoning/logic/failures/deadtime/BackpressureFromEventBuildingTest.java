package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;

public class BackpressureFromEventBuildingTest extends FlowchartCaseTestBase {

    @Test
    public void test01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1507212900008.json.gz");
        assertOnlyOneIsSatisified(backpressureFromEventBuilding, snapshot);
    }
}
