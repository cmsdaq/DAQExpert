package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;

import java.util.*;

public class FedGeneratesDeadtimeTest {
    @Test
    public void satisfied() throws Exception {

        FedGeneratesDeadtime module = new FedGeneratesDeadtime();
        Properties p = new Properties();
        Map<String,Boolean> r = new HashMap<>();
        r.put(FEDDeadtime.class.getSimpleName(),true);
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(),"2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(),"2");
        module.parametrize(p);

        Assert.assertFalse(module.satisfied(mockTestObject(0,0), r));
        Assert.assertFalse(module.satisfied(mockTestObject(2,2), r));
        Assert.assertTrue(module.satisfied(mockTestObject(3,1), r));
        Assert.assertFalse(module.satisfied(mockTestObject(3,3), r));

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