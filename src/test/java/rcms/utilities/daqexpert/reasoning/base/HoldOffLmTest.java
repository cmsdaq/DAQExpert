package rcms.utilities.daqexpert.reasoning.base;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;

import java.util.Map;

public class HoldOffLmTest {

    @Test
    public void satisfiedWithHoldoff() {

        HoldOffLm daqOnFire = new DaqOnFire(1000L);
        Logger.getLogger(HoldOffLm.class).setLevel(Level.DEBUG);

        DAQ daq = new DAQ();


        daq.setDaqState("NotOnFire");
        daq.setLastUpdate(0);
        Assert.assertFalse(daqOnFire.satisfied(daq, null));
        Assert.assertFalse(daqOnFire.satisfiedWithHoldoff(daq, null));


        daq.setDaqState("OnFire");
        daq.setLastUpdate(1000);
        Assert.assertTrue(daqOnFire.satisfied(daq, null));
        Assert.assertFalse(daqOnFire.satisfiedWithHoldoff(daq, null));


        daq.setDaqState("OnFire");
        daq.setLastUpdate(2000);
        Assert.assertTrue(daqOnFire.satisfied(daq, null));
        Assert.assertTrue(daqOnFire.satisfiedWithHoldoff(daq, null));

    }

}

class DaqOnFire extends HoldOffLm {

    public DaqOnFire(Long holdOffPeriod) {
        super(holdOffPeriod);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
        if (daq.getDaqState().equals("OnFire")) {
            return true;
        }
        return false;
    }
}