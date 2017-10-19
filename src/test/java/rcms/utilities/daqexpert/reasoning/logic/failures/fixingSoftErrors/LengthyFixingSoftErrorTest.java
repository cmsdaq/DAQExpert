package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class LengthyFixingSoftErrorTest {
    @Test
    public void satisfied() throws Exception {

        KnownFailure lm = new LengthyFixingSoftError();

        Properties props = new Properties();
        props.setProperty(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey(),"5000");
        DAQ daq = new DAQ();
        List<SubSystem> subsystems = new ArrayList<>();
        SubSystem subsystem = new SubSystem();
        subsystem.setName("TEST");
        subsystems.add(subsystem);
        subsystem.setStatus("FixingSoftError");
        daq.setSubSystems(subsystems);
        daq.setLevelZeroState("Other");
        ((Parameterizable)lm).parametrize(props);

        daq.setLastUpdate(1000);
        Assert.assertFalse(lm.satisfied(daq,null));

        daq.setLevelZeroState("FixingSoftError");

        daq.setLastUpdate(2000);
        Assert.assertFalse(lm.satisfied(daq,null));

        daq.setLastUpdate(6000);
        Assert.assertFalse(lm.satisfied(daq,null));

        daq.setLastUpdate(7000);
        Assert.assertFalse(lm.satisfied(daq,null));

        daq.setLastUpdate(7001);
        Assert.assertTrue(lm.satisfied(daq,null));

        daq.setLastUpdate(8000);
        Assert.assertTrue(lm.satisfied(daq,null));

        daq.setLastUpdate(11000);
        Assert.assertTrue(lm.satisfied(daq,null));

        Assert.assertEquals("Level zero in FixingSoftError longer than 5 sec. This is caused by subsystem(s) <strong>TEST</strong>",lm.getDescriptionWithContext());
    }

}