package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.BeamActive;
import rcms.utilities.daqexpert.reasoning.logic.basic.CriticalDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class DeadtimeFromReTriTest {

    @Test
    public void satisfied() throws Exception {

        DeadtimeFromReTri module = new DeadtimeFromReTri();
        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1510051508327.json.gz");

        Map<String, Boolean> results = new HashMap<>();
        results.put(StableBeams.class.getSimpleName(), true);
        results.put(BeamActive.class.getSimpleName(), true);
        results.put(CriticalDeadtime.class.getSimpleName(), true);

        Properties config = new Properties();
        config.put(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_RETRI.getKey(), "1");

        module.parametrize(config);
        assertEquals(true, module.satisfied(snapshot, results));
    }

}