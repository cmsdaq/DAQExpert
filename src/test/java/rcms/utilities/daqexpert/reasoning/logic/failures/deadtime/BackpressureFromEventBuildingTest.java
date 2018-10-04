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
    public void testDyniarCase() throws URISyntaxException {



        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        final DAQ daq1 = tester.getSnapshot("1538577917939.json.gz");


        Map<String, Output> r = tester.runLogic(daq1, properties);
        r.entrySet().stream().forEach(System.out::println);
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


    @Test
    public void test02() throws URISyntaxException {

        TestBase tester = new TestBase();
        Properties properties = tester.getDefaultProperties();

        tester.runLogic("1534269198968.json.gz");
        Assert.assertEquals(LogicModuleRegistry.BackpressureFromEventBuilding,tester.dominating.getLogicModule());

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
