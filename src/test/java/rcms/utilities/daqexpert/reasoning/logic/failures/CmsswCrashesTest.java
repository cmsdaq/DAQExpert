package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.HltInfo;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class CmsswCrashesTest {


    CmsswCrashes module;
    Map<String, Boolean> results;
    Logger logger = Logger.getLogger(CmsswCrashes.class);

    @Before
    public void prepare() {
        Logger.getLogger(CmsswCrashes.class).setLevel(Level.INFO);
        results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), true);
        module = new CmsswCrashes();

        // mock parameters
        Properties config = new Properties();
        config.put(Setting.EXPERT_CMSSW_CRASHES_THRESHOLD.getKey(), "20");
        config.put(Setting.EXPERT_CMSSW_CRASHES_TIME_WINDOW.getKey(), "20");
        config.put(Setting.EXPERT_CMSSW_CRASHES_HOLDOFF.getKey(), "30");
        module.parametrize(config);
    }

    @Test
    public void simpleThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 0), results));
        Assert.assertTrue(module.satisfied(mockTestObject(1, 21), results));

    }

    @Test
    public void lastMomentInTimeWindowThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(1, 0), results));
        Assert.assertFalse(module.satisfied(mockTestObject(19, 19), results));
        Assert.assertTrue(module.satisfied(mockTestObject(20, 21), results));

    }

    @Test
    public void holdoffOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10), results));
        Assert.assertTrue(module.satisfied(mockTestObject(19, 119), results));
        Assert.assertTrue(module.satisfied(mockTestObject(20, 121), results));

    }

    @Test
    public void slidingWindowThresholdNonOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10), results));
        Assert.assertFalse(module.satisfied(mockTestObject(20, 20), results));
        Assert.assertFalse(module.satisfied(mockTestObject(40, 30), results));

    }

    @Test
    public void slidingWindowThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10), results));
        Assert.assertFalse(module.satisfied(mockTestObject(15, 20), results));
        Assert.assertFalse(module.satisfied(mockTestObject(30, 30), results));
        Assert.assertTrue(module.satisfied(mockTestObject(35, 40), results));

    }

    @Test
    public void holdoffFadesOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(10, 10), results));
        Assert.assertFalse(module.satisfied(mockTestObject(20, 10), results));
        Assert.assertFalse(module.satisfied(mockTestObject(30, 20), results));
        Assert.assertTrue(module.satisfied(mockTestObject(40, 30), results));
        Assert.assertTrue(module.satisfied(mockTestObject(50, 45), results));
        Assert.assertTrue(module.satisfied(mockTestObject(60, 60), results));
        Assert.assertTrue(module.satisfied(mockTestObject(70, 70), results));
        Assert.assertTrue(module.satisfied(mockTestObject(80, 80), results));
        Assert.assertTrue(module.satisfied(mockTestObject(90, 85), results));
        Assert.assertTrue(module.satisfied(mockTestObject(100, 90), results));
        Assert.assertFalse(module.satisfied(mockTestObject(110, 95), results));
        Assert.assertFalse(module.satisfied(mockTestObject(120, 100), results));
        Assert.assertFalse(module.satisfied(mockTestObject(130, 105), results));

    }

    @After
    public void showDescription() {
        logger.info(module.getDescriptionWithContext());
    }

    private DAQ mockTestObject(long timestamp, int crashes) {
        DAQ snapshot = new DAQ();

        snapshot.setLastUpdate(timestamp * 1000);
        snapshot.setHltInfo(new HltInfo());
        snapshot.getHltInfo().setCrashes(crashes);

        return snapshot;
    }

}

