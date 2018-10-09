package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.TestBase;

import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;


public class LinkProblemTest {

    @Test
    public void test() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        final DAQ daq1 = tester.getSnapshot("1538866072902.json.gz");

        LocalDateTime localDateTime =
                Instant.ofEpochMilli(daq1.getLastUpdate()).atZone(ZoneId.of("Europe/Zurich")).toLocalDateTime();


        Map<String, Output> r = tester.runLogic(daq1, properties);

        Output output = r.get("LinkProblem");

        Assert.assertNotNull(output);
        Assert.assertTrue(output.getResult());

        Assert.assertEquals("BPIXP", output.getContext().getTextRepresentation("AFFECTED-PARTITION"));
        Assert.assertEquals("PIXEL", output.getContext().getTextRepresentation("AFFECTED-SUBSYSTEM"));

        Assert.assertEquals("UNKNOWN@FMM, UNKNOWN@APV, BUSY@PM", output.getContext().getTextRepresentation
                ("AFFECTED-TTCP-STATE"));


        // This problem happened outside of working hours (2018-10-07T00:47:52.902)
        System.out.println(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        Assert.assertEquals(Arrays.asList("Red recycle the DAQ (if in stable beams or outside extended working hours)." +
                                                  " Call DAQ on-call and ask him to dump FEROL / FEROL40 registers  " +
                                                  "during extended working hours and outside stable beams."
        ), tester.dominating.getActionSteps());


    }


}