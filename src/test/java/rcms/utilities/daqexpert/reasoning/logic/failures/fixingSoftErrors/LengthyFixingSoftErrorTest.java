package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.*;

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
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLevelZeroState("FixingSoftError");

        daq.setLastUpdate(2000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(6000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(7000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(7001);
        Assert.assertTrue(lm.satisfied(daq));

        daq.setLastUpdate(8000);
        Assert.assertTrue(lm.satisfied(daq));

        daq.setLastUpdate(11000);
        Assert.assertTrue(lm.satisfied(daq));

        ContextHandler.highlightMarkup = false;
        Assert.assertEquals("Level zero in FixingSoftError longer than expected. This is caused by subsystem(s) TEST. The default threshold is 5 s. ",lm.getDescriptionWithContext());
    }

    @Test
    public void testCollectionOfSubsystem() throws Exception {

        KnownFailure lm = new LengthyFixingSoftError();

        Properties props = new Properties();
        props.setProperty(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey(),"5000");
        DAQ daq = new DAQ();
        List<SubSystem> subsystems = new ArrayList<>();
        SubSystem subsystem = new SubSystem();
        subsystem.setName("TEST");
        subsystems.add(subsystem);
        subsystem.setStatus("Running");
        daq.setSubSystems(subsystems);
        daq.setLevelZeroState("Other");
        ((Parameterizable)lm).parametrize(props);

        daq.setLastUpdate(1000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLevelZeroState("FixingSoftError");
        subsystem.setStatus("FixingSoftError");
        daq.setLastUpdate(2000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(7000);
        Assert.assertFalse(lm.satisfied(daq));

        /* Subsystem going back to running will reset its counter - continuous errors will be caught by other LM */
        subsystem.setStatus("Running");
        daq.setLastUpdate(8000);
        Assert.assertFalse(lm.satisfied(daq));

        subsystem.setStatus("FixingSoftError");
        daq.setLastUpdate(9000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(14000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(14001);
        Assert.assertTrue(lm.satisfied(daq));

        ContextHandler.highlightMarkup = false;
        Assert.assertEquals("Level zero in FixingSoftError longer than expected. This is caused by subsystem(s) TEST. The default threshold is 5 s. ",lm.getDescriptionWithContext());
    }

    @Test
    public void subsystemSpecificThreshods() throws Exception {

        KnownFailure lm = new LengthyFixingSoftError();

        Properties props = new Properties();
        props.setProperty(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey(),"5000");
        props.setProperty(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey()+ ".tracker","10000");
        DAQ daq = new DAQ();
        List<SubSystem> subsystems = new ArrayList<>();

        SubSystem subsystem = new SubSystem();
        subsystem.setName("Tracker");
        subsystems.add(subsystem);
        subsystem.setStatus("Running");

        SubSystem subsystem2 = new SubSystem();
        subsystem2.setName("ECAL");
        subsystems.add(subsystem2);
        subsystem2.setStatus("Running");

        daq.setSubSystems(subsystems);
        daq.setLevelZeroState("Other");
        ((Parameterizable)lm).parametrize(props);

        daq.setLastUpdate(1000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLevelZeroState("FixingSoftError");
        subsystem.setStatus("FixingSoftError");
        subsystem2.setStatus("FixingSoftError");
        daq.setLastUpdate(2000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(7000);
        Assert.assertFalse(lm.satisfied(daq));

        daq.setLastUpdate(7001);
        Assert.assertTrue(lm.satisfied(daq));
        Assert.assertEquals(new LinkedHashSet(Arrays.asList("ECAL")),lm.getContextHandler().getContext().get("PROBLEM-SUBSYSTEM"));

        daq.setLastUpdate(8000);
        Assert.assertTrue(lm.satisfied(daq));

        daq.setLastUpdate(12000);
        Assert.assertTrue(lm.satisfied(daq));
        Assert.assertEquals(new LinkedHashSet(Arrays.asList("ECAL")),lm.getContextHandler().getContext().get("PROBLEM-SUBSYSTEM"));

        ContextHandler.highlightMarkup = false;
        Assert.assertEquals("Level zero in FixingSoftError longer than expected. This is caused by subsystem(s) ECAL. The default threshold is 5 s. Note there are subsystem specific threshold(s): [tracker: 10 s]",lm.getDescriptionWithContext());


        daq.setLastUpdate(12001);
        Assert.assertTrue(lm.satisfied(daq));

        Assert.assertEquals(new LinkedHashSet(Arrays.asList("ECAL", "Tracker")),lm.getContextHandler().getContext().get("PROBLEM-SUBSYSTEM"));

        Assert.assertEquals("Level zero in FixingSoftError longer than expected. This is caused by subsystem(s) [ECAL, Tracker]. The default threshold is 5 s. Note there are subsystem specific threshold(s): [tracker: 10 s]",lm.getDescriptionWithContext());
    }

}