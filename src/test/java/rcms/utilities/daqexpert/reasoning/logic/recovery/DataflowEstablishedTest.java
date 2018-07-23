package rcms.utilities.daqexpert.reasoning.logic.recovery;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataflowEstablishedTest {

    @Test
    public void test(){

        DataflowEstablished lm = new DataflowEstablished();

        Properties p = new Properties();
        p.put(Setting.DATAFLOW_ESTABLISHED.getKey(), "2");
        lm.parametrize(p);

        Map<String, Output> results = new HashMap<>();

        results.put(NoRate.class.getSimpleName(), new Output(false));

        Assert.assertFalse(lm.satisfied(getSnaphot(0L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(1L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(2L),results));
        Assert.assertTrue(lm.satisfied(getSnaphot(3L),results));
        Assert.assertTrue(lm.satisfied(getSnaphot(4L),results));
        Assert.assertTrue(lm.satisfied(getSnaphot(5L),results));
        Assert.assertTrue(lm.satisfied(getSnaphot(6L),results));

        results.put(NoRate.class.getSimpleName(), new Output(true));
        Assert.assertTrue(lm.satisfied(getSnaphot(7L),results));
        Assert.assertTrue(lm.satisfied(getSnaphot(8L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(9L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(10L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(11L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(12L),results));

        results.put(NoRate.class.getSimpleName(), new Output(false));
        Assert.assertFalse(lm.satisfied(getSnaphot(0L),results));
        results.put(NoRate.class.getSimpleName(), new Output(true));


    }

    @Test
    public void neverOnTest(){

        DataflowEstablished lm = new DataflowEstablished();

        Properties p = new Properties();
        p.put(Setting.DATAFLOW_ESTABLISHED.getKey(), "2");
        lm.parametrize(p);

        Map<String, Output> results = new HashMap<>();

        results.put(NoRate.class.getSimpleName(), new Output(false));
        Assert.assertFalse(lm.satisfied(getSnaphot(0L),results));
        results.put(NoRate.class.getSimpleName(), new Output(true));
        Assert.assertFalse(lm.satisfied(getSnaphot(1L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(2L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(3L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(4L),results));
        Assert.assertFalse(lm.satisfied(getSnaphot(5L),results));


    }

    private DAQ getSnaphot(Long timestamp){
        DAQ daq = new DAQ();

        daq.setLastUpdate(timestamp);

        return daq;
    }

}