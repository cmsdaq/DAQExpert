package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.*;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.failures.TestBase;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.*;

public class BackpressureFromEventBuildingTest {

    @Test
    public void test() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        final DAQ daq1 = tester.getSnapshot("1534269198968.json.gz");

        System.out.println("upgraded: " +isUpgraded(daq1.getFeds().stream().filter(f->1386==f.getSrcIdExpected()).findFirst().get()));

        Map<String, Output> r = tester.runLogic(daq1, properties);

        Assert.assertTrue(r.get("BackpressureFromEventBuilding").getResult());
        Output output = r.get("BackpressureFromEventBuilding");

        Assert.assertEquals("1386", output.getContext().getTextRepresentation("PROBLEMATIC-FED"));


        //Assert.assertEquals("", tester.dominating);

    }


    @Test
    public void test02() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        Map<String, Output> r = tester.runLogic("1534269198968.json.gz");

        Assert.assertTrue(r.get("BackpressureFromEventBuilding").getResult());
        Output output = r.get("BackpressureFromEventBuilding");

        Assert.assertEquals("1386", output.getContext().getTextRepresentation("PROBLEMATIC-FED"));

        Assert.assertEquals(LogicModuleRegistry.BackpressureFromEventBuilding,tester.dominating.getLogicModule());

    }

    /*
    1538577612360 2018-10-03T16:40:12+02:00
    1538577699073 2018-10-03T16:41:39+02:00
    1538577956916 2018-10-03T16:45:56+02:00
    1538578074626 2018-10-03T16:47:54+02:00
     */
    @Test
    public void testDyniarCase() throws URISyntaxException {



        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        final DAQ daq0 = tester.getSnapshot("1538577940601.json.gz"); // 2018-10-03T16:45:40+02:00
        final DAQ daq1 = tester.getSnapshot("1538577917939.json.gz"); // 2018-10-03T16:45:17+02:00
        final DAQ daq2 = tester.getSnapshot("1538577962206.json.gz"); // 2018-10-03T16:46:02+02:00


        Map<String, Output> r0 = tester.runLogic(daq0, properties);
        r0.entrySet().stream().forEach(System.out::println);
        Assert.assertThat(r0.keySet(), hasItem(is("BackpressureFromEventBuilding")));

        Map<String, Output> r1 = tester.runLogic(daq1, properties);
        r1.entrySet().stream().forEach(System.out::println);
        Assert.assertThat(r1.keySet(), hasItem(is("BackpressureFromEventBuilding")));
        Output output = r1.get("BackpressureFromEventBuilding");
        Assert.assertEquals("1386", output.getContext().getTextRepresentation("PROBLEMATIC-FED"));


        Map<String, Output> r2 = tester.runLogic(daq2, properties);
        r2.entrySet().stream().forEach(System.out::println);
        Assert.assertFalse(r2.get("BackpressureFromEventBuilding").getResult()); // Here the requests on RU are non-0, 2

    }



    @Test
    public void testBackpressurFromEVBWithLegacyFed() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        /* Smooth datataking here - DAQ object modified from this point */
        final DAQ daq1 = tester.getSnapshot("1534729731786.json.gz");
        fakeEvmBackpressure(daq1,849,"ru-c2e12-30-01.cms", true);

        Map<String, Output> r = tester.runLogic(daq1, properties);
        System.out.println(tester.dominating);

        Assert.assertThat(r.keySet(), hasItem(is("BackpressureFromEventBuilding")));
        Output output = r.get("BackpressureFromEventBuilding");

        Assert.assertEquals("849", output.getContext().getTextRepresentation("PROBLEMATIC-FED"));

    }

    @Test
    public void testBackpressurFromEVBonUpgradedFed() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        /* Smooth datataking here - DAQ object modified from this point */
        final DAQ daq2 = tester.getSnapshot("1534729731786.json.gz");
        fakeEvmBackpressure(daq2,1104,"ru-c2e14-13-01.cms", true);

        Map<String, Output> r = tester.runLogic(daq2, properties);
        System.out.println(tester.dominating);

        Assert.assertThat(r.keySet(), hasItem(is("BackpressureFromEventBuilding")));
        Output output = r.get("BackpressureFromEventBuilding");

        Assert.assertEquals("1104", output.getContext().getTextRepresentation("PROBLEMATIC-FED"));

    }

    /**
     * A case where both legacy and upgraded FEDs are backpressured by EVB.
     *
     * But there are also some FEDs (both upgraded and legacy) that are problematic - but not behing problematic RU
     */
    @Test
    public void testCollectionOfFeds() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        /* Smooth datataking here - DAQ object modified from this point */
        final DAQ daq2 = tester.getSnapshot("1534729731786.json.gz");
        fakeEvmBackpressure(daq2,1104,"ru-c2e14-13-01.cms", true);
        fakeEvmBackpressure(daq2,849,"ru-c2e12-30-01.cms", false); // 849 should not be reported


        Map<String, Output> r = tester.runLogic(daq2, properties);
        System.out.println(tester.dominating);

        Assert.assertThat(r.keySet(), hasItem(is("BackpressureFromEventBuilding")));
        Output output = r.get("BackpressureFromEventBuilding");

        Assert.assertEquals("1104", output.getContext().getTextRepresentation("PROBLEMATIC-FED"));

    }



    private void fakeEvmBackpressure(DAQ daq, int fedNumber, String ruHostname, boolean fakeOnRu){


        RU ru = daq.getRus().stream().filter(r->ruHostname.equalsIgnoreCase(r.getHostname())).findFirst().orElseThrow(RuntimeException::new);
        RU evm = daq.getRus().stream().filter(r->r.isEVM()).findFirst().orElseThrow(RuntimeException::new);

        FED fed = ru.getFEDs(false).stream().filter(f->f.getSrcIdExpected() == fedNumber).findFirst().orElseThrow(RuntimeException::new);

        System.out.println("Faking EVB backpressure on FED" + fedNumber + ", which is " + (isUpgraded(fed)?"upgraded":"legacy"));
        if(!isUpgraded(fed)){
            // fake missing per fed deadtime information
            fed.setPercentBusy(50);
        }
        fed.setPercentBackpressure(50);

        daq.getTcdsGlobalInfo().getDeadTimesInstant()
                .put("beamactive_total",50d);
        daq.getTcdsGlobalInfo().getDeadTimesInstant()
                .put("beamactive_tts", 50d);

        if(fakeOnRu) {
            ru.setFragmentsInRU(256);
            ru.setRequests(0);
        }else{

            ru.setFragmentsInRU(100);
            ru.setRequests(100);
        }
        evm.setRequests(99);

    }


    private boolean isUpgraded(FED fed){

        FMM fmm = null;
        if(!fed.isFmmMasked()){
            fmm = fed.getFmm();
        }else  {
            if(fed.getDependentFeds().size() ==1){
                FED dep = fed.getDependentFeds().iterator().next();
                if(!dep.isFmmMasked()){
                    fmm = dep.getFmm();
                }

            }
        }


        if(fmm != null && fmm.getFmmType() != null) {

            switch(fmm.getFmmType()){
                case fmm:
                    return false;
                case pi:
                case amc13:
                    return true;
                default:
                    return false;
            }
        }else {
            return false;
        }
    }

}
