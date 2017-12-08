package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.RateTooHigh;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.*;

public class FedDeadtimeDueToDaqTest {

    /**
     * module under test
     */
    FedDeadtimeDueToDaq module;

    /**
     * outputs of other LMs used by LM under test
     */
    Map<String, Output> results;

    Logger logger = Logger.getLogger(FedDeadtimeDueToDaqTest.class);


    @Before
    public void prepareForTest() {
        module = new FedDeadtimeDueToDaq();
        results = new HashMap<>();
        results.put(FEDDeadtime.class.getSimpleName(), new Output(true));

        Properties config = new Properties();
        config.put(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        config.put(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        module.parametrize(config);
    }

    @Test
    public void test01() throws URISyntaxException {
        assertTrue(module.satisfied(FlowchartCaseTestBase.getSnapshot("1507212900008.json.gz"), results));
        logger.info(module.getDescriptionWithContext());
        assertEquals("FED <strong>622</strong> has a deadtime <strong>5.2%</strong>, due to DAQ backpressure <strong>5.2%</strong>. The threshold for deadtime is 2.0%, backpressure: 2.0%", module.getDescriptionWithContext());
    }

    @Test
    public void test03() throws URISyntaxException {
        assertTrue(module.satisfied(FlowchartCaseTestBase.getSnapshot("1507212240143.json.gz"), results));
        logger.info(module.getDescriptionWithContext());
        assertEquals("FED <strong>359</strong> has a deadtime <strong>4.4%</strong>, due to DAQ backpressure <strong>4.4%</strong>. The threshold for deadtime is 2.0%, backpressure: 2.0%", module.getDescriptionWithContext());

    }

    @Test
    public void pseudoFedHierarchyTest() throws Exception {

        FedDeadtimeDueToDaq module = new FedDeadtimeDueToDaq();
        Properties p = new Properties();
        Map<String, Output> r = new HashMap<>();
        r.put(FEDDeadtime.class.getSimpleName(), new Output(true));
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(p);

        DAQ snapshot = new DAQ();
        TTCPartition partition = new TTCPartition();
        Set<FED> feds = new HashSet<>();

        FED pseudoFed = mockTestObject(10000, 10,0);
        FED fed = mockTestObject(1, 0,10);
        fed.setDependentFeds(Arrays.asList(pseudoFed));
        feds.add(fed);
        partition.setFeds(new ArrayList<>(feds));

        Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(partition);
        snapshot.setFeds(feds);
        snapshot.getFeds();

        snapshot.setTtcPartitions(Arrays.asList(partition));
        Assert.assertTrue(module.satisfied(snapshot, r));

    }

    @Test
    public void satisfied() throws Exception {

        FedDeadtimeDueToDaq module = new FedDeadtimeDueToDaq();
        Properties p = new Properties();
        Map<String, Output> r = new HashMap<>();
        r.put(FEDDeadtime.class.getSimpleName(), new Output(true));
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(p);

        Assert.assertFalse(module.satisfied(mockTestObject(0, 0), r));
        Assert.assertFalse(module.satisfied(mockTestObject(2, 2), r));
        Assert.assertTrue(module.satisfied(mockTestObject(3, 3), r));
        logger.info(module.getDescriptionWithContext());
        Assert.assertFalse(module.satisfied(mockTestObject(3, 1), r));

    }


    private DAQ mockTestObject(float deadtime, float backpressure) {
        DAQ snapshot = new DAQ();
        Set<FED> feds = new HashSet<>();
        feds.add(mockTestObject(1, 0, 0));
        feds.add(mockTestObject(2, deadtime, backpressure));
        feds.add(mockTestObject(3, 0, 0));
        snapshot.setFeds(feds);

        TTCPartition p = new TTCPartition();
        p.setName("testpartition");
        p.setFeds(new ArrayList<>(feds));
        snapshot.setTtcPartitions(Arrays.asList(p));
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