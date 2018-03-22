package rcms.utilities.daqexpert.jobs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class RecoveryJobPerformerTest {

    @Test
    public void test(){

        RecoveryJobPerformer job = new RecoveryJobPerformer();
        RecoveryRequest r = new RecoveryRequest();

        Set<String> list = new HashSet<>();
        list.add("ECAL");
        r.setRedRecycle(list);
        r.setProblemDescription("Test problem");

        Long id =  job.sendRequest(r);

        System.out.println("Request created with id " + id);

        job.checkStatus(id);
    }

}