package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

public class FedStuckDueToDaqTest extends FlowchartCaseTestBase {

    @Test
    public void test() throws URISyntaxException {

        DAQ daq = getSnapshot("1531900145520.json.gz");

        FedStuckDueToDaq lm = new FedStuckDueToDaq();


        Map<String, Output> r = new HashMap<>();

        boolean output = lm.satisfied(daq,r);

        Assert.assertTrue(output);

        ContextHandler context = lm.getContextHandler();
        assertEquals(new HashSet(Arrays.asList("HCAL")), context.getContext().get("PROBLEM-SUBSYSTEM"));
        assertEquals(new HashSet(Arrays.asList("HBHEC")), context.getContext().get("PROBLEM-PARTITION"));
        assertEquals(new HashSet(Arrays.asList("(1116 behind pseudo FED 11116)", "(1117 behind pseudo FED 11116)", "(1113 behind pseudo FED 11112)", "(1115 behind pseudo FED 11114)")), context.getContext().get("PROBLEM-FED"));

        ContextHandler.highlightMarkup = false;
        assertEquals("The run is blocked by HCAL/HBHEC/[(1113 behind pseudo FED 11112), (1115 behind pseudo FED 11114), (1116 behind pseudo FED 11116), (1117 behind pseudo FED 11116)]. " +
                "It receives backpressure from the DAQ. " +
                "There is nothing wrong with this FED, the problem is in the DAQ or downstream. " +
                "There is typically another condition active for the problem in the DAQ", lm.getDescriptionWithContext());


    }

}