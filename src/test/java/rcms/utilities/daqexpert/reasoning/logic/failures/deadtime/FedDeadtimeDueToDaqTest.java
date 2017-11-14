package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.RateTooHigh;

import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.*;

public class FedDeadtimeDueToDaqTest {

    /** module under test */
    FedDeadtimeDueToDaq module;

    /** outputs of other LMs used by LM under test */
    Map<String, Boolean> results;


    @Before
    public void prepareForTest(){
        module = new FedDeadtimeDueToDaq();
        results = new HashMap<>();
        results.put(FEDDeadtime.class.getSimpleName(), true);

        Properties config = new Properties();
        config.put(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        config.put(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        module.parametrize(config);
    }

    @Test
    public void test01() throws URISyntaxException {
        assertTrue(module.satisfied(FlowchartCaseTestBase.getSnapshot("1507212900008.json.gz"), results));
    }

    @Test
    public void test02() throws URISyntaxException {
        assertTrue(module.satisfied(FlowchartCaseTestBase.getSnapshot("1480508609145.json.gz"), results));
    }

    @Test
    public void test03() throws URISyntaxException {
        assertTrue(module.satisfied(FlowchartCaseTestBase.getSnapshot("1507212240143.json.gz"), results));
    }


    @Test
    public void satisfied() throws Exception {

        FedDeadtimeDueToDaq module = new FedDeadtimeDueToDaq();
        Properties p = new Properties();
        Map<String, Boolean> r = new HashMap<>();
        r.put(FEDDeadtime.class.getSimpleName(), true);
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(p);

        Assert.assertFalse(module.satisfied(mockTestObject(0, 0), r));
        Assert.assertFalse(module.satisfied(mockTestObject(2, 2), r));
        Assert.assertTrue(module.satisfied(mockTestObject(3, 3), r));
        Assert.assertFalse(module.satisfied(mockTestObject(3, 1), r));

    }

    private DAQ mockTestObject(float deadtime, float backpressure) {
        DAQ snapshot = new DAQ();
        Set<FED> feds = new HashSet<>();
        feds.add(mockTestObject(1, 0, 0));
        feds.add(mockTestObject(2, deadtime, backpressure));
        feds.add(mockTestObject(3, 0, 0));
        snapshot.setFeds(feds);
        return snapshot;
    }

    private FED mockTestObject(int id, float deadtime, float backpressure) {
        FED fed = new FED();
        fed.setSrcIdExpected(id);
        fed.setPercentBackpressure(backpressure);
        fed.setPercentBusy(deadtime);
        return fed;
    }
}