package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.TestBase;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;


public class LinkProblemTest {

    @Test
    public void test() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        final DAQ daq1 = tester.getSnapshot("1538866072902.json.gz");

        Map<String, Output> r = tester.runLogic(daq1, properties);

        Output output = r.get("LinkProblem");

        Assert.assertNotNull(output);
        Assert.assertTrue(output.getResult());

        Assert.assertEquals("BPIXP", output.getContext().getTextRepresentation("AFFECTED-PARTITION"));
        Assert.assertEquals("PIXEL", output.getContext().getTextRepresentation("AFFECTED-SUBSYSTEM"));

        Assert.assertEquals("UNKNOWN@FMM, UNKNOWN@APV, BUSY@PM", output.getContext().getTextRepresentation("AFFECTED-TTCP-STATE"));



        //Assert.assertEquals("", tester.dominating);

    }


}