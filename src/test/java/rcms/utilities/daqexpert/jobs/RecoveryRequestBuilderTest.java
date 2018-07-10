package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockserver.model.Action;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RecoveryRequestBuilderTest {

    @Test
    public void checkAutomatedRecoveriesMarkupInLM(){
        List<ActionLogicModule> r = LogicModuleRegistry.getModulesInRunOrder()
                .stream().filter(l->l instanceof ActionLogicModule).map(l->(ActionLogicModule)l).collect(Collectors.toList());


        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();

        for(ActionLogicModule lm : r){


            try {
                List<String> fakeContextSteps = lm.getActionWithContextRawRecovery();

                if(fakeContextSteps != null) {
                    fakeContextSteps = fakeContextSteps.stream().map(s -> s.replaceAll("\\{\\{.*\\}\\}", "X")).collect(Collectors.toList());

                    RecoveryRequest rr = recoveryRequestBuilder.buildRecoveryRequest(fakeContextSteps, "", "", 1L);

                    if (rr != null && rr.getRecoverySteps().size() > 0) {
                        System.out.println("Name: " + lm.getName());
                        //System.out.println("Rec:  " + lm.getActionWithContextRawRecovery());
                        System.out.println(rr);
                        System.out.println("---");
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                Assert.fail("Could not build recovery for lm: " + lm.getName());
            }

        }
    }

    @Test
    public void test(){

        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<RedRecycle::ECAL>> to fix");}};
        List<List<Pair<RecoveryJob,List<String>>>> jobs = recoveryRequestBuilder.getJobs(steps);
        Assert.assertEquals(1, jobs.size());
        Assert.assertEquals(1, jobs.iterator().next().size());
        Assert.assertEquals(RecoveryJob.RedRecycle, jobs.iterator().next().iterator().next().getLeft());
        Assert.assertEquals("[ECAL]", jobs.iterator().next().iterator().next().getRight().toString());

    }

    @Test
    public void testNoArgs(){

        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<StopAndStartTheRun>> to fix");}};
        List<List<Pair<RecoveryJob,List<String>>>> jobs = recoveryRequestBuilder.getJobs(steps);
        Assert.assertEquals(1, jobs.size());
        Assert.assertEquals(1, jobs.iterator().next().size());
        Assert.assertEquals(RecoveryJob.StopAndStartTheRun, jobs.iterator().next().iterator().next().getLeft());


    }

    @Test
    public void multipleInOneStep(){

        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
        List<String> steps = new ArrayList<String>(){{add("Do <<RedRecycle::ECAL>> and <<GreenRecycle::ECAL>>");}};
        List<List<Pair<RecoveryJob,List<String>>>>  jobs = recoveryRequestBuilder.getJobs(steps);
        Assert.assertEquals(1, jobs.size());
        Assert.assertEquals(2, jobs.iterator().next().size());

        Assert.assertEquals(RecoveryJob.RedRecycle, jobs.iterator().next().iterator().next().getLeft());
        Assert.assertEquals("[ECAL]", jobs.iterator().next().iterator().next().getRight().toString());

    }

    @Test
    public void manyStepsTest(){

        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
        List<String> steps = new ArrayList<String>(){{add("Do <<GreenRecycle::TRACKER>>");add("Than do <<GreenRecycle::ES>>");}};
        List<List<Pair<RecoveryJob,List<String>>>>  jobs = recoveryRequestBuilder.getJobs(steps);
        Assert.assertEquals(2, jobs.size());
        Assert.assertEquals(1, jobs.iterator().next().size());

        Assert.assertEquals(RecoveryJob.GreenRecycle, jobs.iterator().next().iterator().next().getLeft());
        Assert.assertEquals("[TRACKER]", jobs.iterator().next().iterator().next().getRight().toString());
    }

    @Test
    public void testRecoveriy(){
        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<RedRecycle::ECAL>> to fix");}};
        RecoveryRequest recovery = recoveryRequestBuilder.buildRecoveryRequest(steps,steps,"","",0L);

        Assert.assertEquals(1, recovery.getRecoverySteps().size());
        RecoveryStep rr = recovery.getRecoverySteps().iterator().next();

        Assert.assertEquals(1, rr.getRedRecycle().size());
        Assert.assertEquals("ECAL",rr.getRedRecycle().iterator().next());

    }
    @Test
    public void multipleContextTest(){
        RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<RedRecycle::[ECAL,TRACKER]>> to fix");}};
        RecoveryRequest recovery = recoveryRequestBuilder.buildRecoveryRequest(steps, steps,"","",0L);

        Assert.assertEquals(1, recovery.getRecoverySteps().size());
        RecoveryStep rr = recovery.getRecoverySteps().iterator().next();

        Assert.assertEquals(2, rr.getRedRecycle().size());
        Iterator<String> it = rr.getRedRecycle().iterator();
        Assert.assertEquals("TRACKER",it.next());
        Assert.assertEquals("ECAL",it.next());

    }
}