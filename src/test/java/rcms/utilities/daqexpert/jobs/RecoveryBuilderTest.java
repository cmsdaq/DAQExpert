package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecoveryBuilderTest {

    @Test
    public void test(){

        RecoveryBuilder recoveryBuilder = new RecoveryBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<RedRecycle::ECAL>> to fix");}};
        List<List<Pair<Jobs,List<String>>>> jobs = recoveryBuilder.getJobs(steps);
        Assert.assertEquals(1, jobs.size());
        Assert.assertEquals(1, jobs.iterator().next().size());
        Assert.assertEquals(Jobs.RedRecycle, jobs.iterator().next().iterator().next().getLeft());
        Assert.assertEquals("[ECAL]", jobs.iterator().next().iterator().next().getRight().toString());

    }

    @Test
    public void multipleInOneStep(){

        RecoveryBuilder recoveryBuilder = new RecoveryBuilder();
        List<String> steps = new ArrayList<String>(){{add("Do <<RedRecycle::ECAL>> and <<GreenRecycle::ECAL>>");}};
        List<List<Pair<Jobs,List<String>>>>  jobs = recoveryBuilder.getJobs(steps);
        Assert.assertEquals(1, jobs.size());
        Assert.assertEquals(2, jobs.iterator().next().size());

        Assert.assertEquals(Jobs.RedRecycle, jobs.iterator().next().iterator().next().getLeft());
        Assert.assertEquals("[ECAL]", jobs.iterator().next().iterator().next().getRight().toString());

    }

    @Test
    public void manyStepsTest(){

        RecoveryBuilder recoveryBuilder = new RecoveryBuilder();
        List<String> steps = new ArrayList<String>(){{add("Do <<GreenRecycle::TRACKER>>");add("Than do <<GreenRecycle::ES>>");}};
        List<List<Pair<Jobs,List<String>>>>  jobs = recoveryBuilder.getJobs(steps);
        Assert.assertEquals(2, jobs.size());
        Assert.assertEquals(1, jobs.iterator().next().size());

        Assert.assertEquals(Jobs.GreenRecycle, jobs.iterator().next().iterator().next().getLeft());
        Assert.assertEquals("[TRACKER]", jobs.iterator().next().iterator().next().getRight().toString());
    }

    @Test
    public void testRecoveriy(){
        RecoveryBuilder recoveryBuilder = new RecoveryBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<RedRecycle::ECAL>> to fix");}};
        List<RecoveryRequest> recovery = recoveryBuilder.getRecoveries(steps,"");

        Assert.assertEquals(1, recovery.size());
        RecoveryRequest rr = recovery.iterator().next();

        Assert.assertEquals(1, rr.getRedRecycle().size());
        Assert.assertEquals("ECAL",rr.getRedRecycle().iterator().next());

    }
    @Test
    public void multipleContextTest(){
        RecoveryBuilder recoveryBuilder = new RecoveryBuilder();
        List<String> steps = new ArrayList<String>(){{add("D <<RedRecycle::[ECAL,TRACKER]>> to fix");}};
        List<RecoveryRequest> recovery = recoveryBuilder.getRecoveries(steps,"");

        Assert.assertEquals(1, recovery.size());
        RecoveryRequest rr = recovery.iterator().next();

        Assert.assertEquals(2, rr.getRedRecycle().size());
        Iterator<String> it = rr.getRedRecycle().iterator();
        Assert.assertEquals("TRACKER",it.next());
        Assert.assertEquals("ECAL",it.next());

    }
}