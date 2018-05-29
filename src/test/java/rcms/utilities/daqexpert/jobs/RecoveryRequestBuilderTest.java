package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RecoveryRequestBuilderTest {

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