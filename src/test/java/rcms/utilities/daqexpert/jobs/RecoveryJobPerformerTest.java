package rcms.utilities.daqexpert.jobs;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class RecoveryJobPerformerTest {


    @BeforeClass
    public static void prepareControllerStub() {
        MockServerClient mockServer = startClientAndServer(8082);
        mockServer.when(request().withMethod("POST").withPath("/recover"), exactly(1))
                .respond(
                        response().withStatusCode(201)
                                .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")).withBody("1")
                                .withDelay(new Delay(SECONDS, 1)));

        mockServer.when(request().withMethod("GET").withPath("/status/1/"), exactly(1))
                .respond(
                        response().withStatusCode(201)
                                .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")).withBody("awaiting approval")
                                .withDelay(new Delay(SECONDS, 1)));

        //ConditionProducer.enableMarkup = false;
    }

    @Test
    public void test(){

        RecoveryJobPerformer job = new RecoveryJobPerformer();
        RecoveryRequest r = new RecoveryRequest();

        Set<String> list = new HashSet<>();
        list.add("ECAL");
        r.setRedRecycle(list);
        r.setProblemDescription("Test problem");

        Long id =  job.sendRequest(r);
        Assert.assertEquals(new Long(1),id);

        String status = job.checkStatus(id);
        Assert.assertEquals("awaiting approval",status);


    }

}