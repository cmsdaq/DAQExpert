package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;

public class BackpressureFromEventBuildingTest extends FlowchartCaseTestBase {

    @Test
    @Ignore
    //FIXME this is a test when fed-pseudo fed relationship is reveals fail of  Backpressure from event building
    public void test01() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1507212900008.json.gz");

        Logger.getLogger(BackpressureFromEventBuilding.class).setLevel(Level.INFO);
        Logger.getLogger(BackpressureFromFerol.class).setLevel(Level.INFO);
        Logger.getLogger(FedDeadtimeDueToDaq.class).setLevel(Level.INFO);


        FedDeadtimeDueToDaq f = new FedDeadtimeDueToDaq();


        /* This is expected to have satisfied two cases here. Confirmed with Remi and Andre*/
        assertSatisfiedLogicModules(snapshot, backpressureFromEventBuilding, backpressureFromFerol);
        //assertOnlyOneIsSatisified(backpressureFromEventBuilding, snapshot);
    }

    @Test
    public void test02() throws URISyntaxException {
        DAQ snapshot = getSnapshot("1534269198968.json.gz");

        Logger.getLogger(BackpressureFromEventBuilding.class).setLevel(Level.INFO);
        Logger.getLogger(BackpressureFromFerol.class).setLevel(Level.INFO);
        Logger.getLogger(FedDeadtimeDueToDaq.class).setLevel(Level.INFO);


        ContextHandler.highlightMarkup=false;
        assertSatisfiedLogicModules(snapshot, backpressureFromEventBuilding);

        System.out.println(backpressureFromEventBuilding.getDescriptionWithContext());
        System.out.println(backpressureFromEventBuilding.getActionWithContext());
    }
}
