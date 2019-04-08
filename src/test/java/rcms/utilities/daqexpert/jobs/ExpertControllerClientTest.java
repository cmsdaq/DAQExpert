package rcms.utilities.daqexpert.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ExpertControllerClientTest {


    @BeforeClass
    public static void prepareControllerStub() throws JsonProcessingException {
        MockServerClient mockServer = startClientAndServer(28082);
        RecoveryResponse recoveryResponse = new RecoveryResponse();
        ObjectMapper om = new ObjectMapper();
        String body = om.writeValueAsString(recoveryResponse);
        mockServer.when(request().withMethod("POST").withPath("/recover"), exactly(1))
                .respond(
                        response().withStatusCode(201)
                                .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")).withBody(body)
                                .withDelay(new Delay(SECONDS, 1)));

        mockServer.when(request().withMethod("GET").withPath("/status/"), exactly(1))
                .respond(
                        response().withStatusCode(201)
                                .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400")).withBody("awaiting approval")
                                .withDelay(new Delay(SECONDS, 1)));

        //ConditionProducer.enableMarkup = false;
    }

    @Test
    public void test(){

        ExpertControllerClient job = new ExpertControllerClient("http://localhost:28082");
        RecoveryRequest r = new RecoveryRequest();

        Set<String> list = new HashSet<>();
        list.add("ECAL");

        RecoveryRequestStep step = new RecoveryRequestStep();
        step.setRedRecycle(list);
        r.setProblemDescription("Test problem");
        r.setRecoveryRequestSteps(Arrays.asList(step));

        RecoveryResponse response =  job.sendRecoveryRequest(r);
        Assert.assertNotNull(response);

    }

}