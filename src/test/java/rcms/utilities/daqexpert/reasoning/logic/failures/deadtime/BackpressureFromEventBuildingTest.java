package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;

public class BackpressureFromEventBuildingTest extends FlowchartCaseTestBase {

    @Test
    public void test01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1507212900008.json.gz");

        Logger.getLogger(BackpressureFromEventBuilding.class).setLevel(Level.INFO);
        Logger.getLogger(BackpressureFromFerol.class).setLevel(Level.INFO);

        /* This is expected to have satisfied two cases here. Confirmed with Remi and Andre*/
        assertSatisfiedLogicModules(snapshot, backpressureFromEventBuilding, backpressureFromFerol);
        //assertOnlyOneIsSatisified(backpressureFromEventBuilding, snapshot);
    }
}
