package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.ArrayList;
import java.util.List;


public class StuckAfterSoftErrorTest {

    @Test
    public void satisfied() throws Exception {
        KnownFailure lm = new StuckAfterSoftError();
        Logger.getLogger(StuckAfterSoftError.class).setLevel(Level.ALL);


        List<SubSystem> subsystems = new ArrayList<>();
        SubSystem problemSubsystem = new SubSystem();
        problemSubsystem.setName("TEST");
        problemSubsystem.setStatus("Error");
        subsystems.add(problemSubsystem);

        DAQ daq = new DAQ();
        daq.setSubSystems(subsystems);
        Assert.assertFalse(lm.satisfied(daq, null));

        daq.setLevelZeroState("FixingSoftError");

        Assert.assertFalse(lm.satisfied(daq, null));

        daq.setLevelZeroState("Error");

        Assert.assertTrue("Satisfied when preceding state to Error is FixingSoftError", lm.satisfied(daq, null));
        Assert.assertTrue(lm.satisfied(daq, null));

        Assert.assertEquals("Level zero is stuck after fixing soft error. This is caused by subsystem(s) <strong>TEST</strong>", lm.getDescriptionWithContext());

        daq.setLevelZeroState("Other");
        Assert.assertFalse(lm.satisfied(daq, null));


        daq.setLevelZeroState("Error");
        Assert.assertFalse("Will not fire if preceding state is other than FixingSoftError", lm.satisfied(daq, null));
    }

}