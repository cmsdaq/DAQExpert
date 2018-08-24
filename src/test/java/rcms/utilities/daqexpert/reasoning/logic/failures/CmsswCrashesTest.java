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
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.ResultSupplier;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class CmsswCrashesTest {


    CmsswCrashes module;
    ResultSupplier resultSupplier = new ResultSupplier();
    Logger logger = Logger.getLogger(CmsswCrashes.class);

    @Before
    public void prepare() {
        Logger.getLogger(CmsswCrashes.class).setLevel(Level.INFO);

        resultSupplier.clear();
        resultSupplier.update(LogicModuleRegistry.StableBeams, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BackpressureFromHlt, new Output(true));

        module = new CmsswCrashes();
        module.setResultSupplier(resultSupplier);

        // mock parameters
        Properties config = new Properties();
        config.put(Setting.EXPERT_CMSSW_CRASHES_THRESHOLD.getKey(), "20");
        config.put(Setting.EXPERT_CMSSW_CRASHES_TIME_WINDOW.getKey(), "20");
        module.parametrize(config);
    }

    @Test
    public void simpleThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 0)));
        Assert.assertTrue(module.satisfied(mockTestObject(1, 21)));

    }

    @Test
    public void lastMomentInTimeWindowThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(1, 0)));
        Assert.assertFalse(module.satisfied(mockTestObject(19, 19)));
        Assert.assertTrue(module.satisfied(mockTestObject(20, 21)));

    }

    @Test
    public void slidingWindowThresholdNonOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10)));
        Assert.assertFalse(module.satisfied(mockTestObject(20, 20)));
        Assert.assertFalse(module.satisfied(mockTestObject(40, 30)));

    }

    @Test
    public void slidingWindowThresholdOverrunTest() throws URISyntaxException {

        Assert.assertFalse(module.satisfied(mockTestObject(0, 10)));
        Assert.assertFalse(module.satisfied(mockTestObject(15, 20)));
        Assert.assertFalse(module.satisfied(mockTestObject(30, 30)));
        Assert.assertTrue(module.satisfied(mockTestObject(35, 40)));

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

