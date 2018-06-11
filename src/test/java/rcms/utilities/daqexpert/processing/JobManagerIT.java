package rcms.utilities.daqexpert.processing;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.*;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.events.ConditionEventResource;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.jobs.*;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.segmentation.DataResolution;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;

public class JobManagerIT {

    private static final Logger logger = Logger.getLogger(JobManagerIT.class);
    private List<Condition> conditionsYielded;

    private List<RecoveryRequest> recoveryRequestsYielded;

    private List<ConditionEventResource> notifications;
    @BeforeClass
    public static void prepareNMStub() {
        MockServerClient mockServer = startClientAndServer(18081);
        mockServer.when(request().withMethod("POST").withPath("/nm/rest/events/"))
                .respond(
                        response().withStatusCode(201)
                                .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withDelay(new Delay(SECONDS, 1)));

        //ConditionProducer.enableMarkup = false;
    }


    private void runOverTestPeriod(String startDateString, String endDateString, EventSender eventSender)
            throws InterruptedException {

        Application.get().getProp().setProperty(Setting.PROCESSING_START_DATETIME.getKey(), startDateString);
        Application.get().getProp().setProperty(Setting.PROCESSING_END_DATETIME.getKey(), endDateString);
        DataManager dataManager = new DataManager();
        String sourceDirectory = Application.get().getProp(Setting.SNAPSHOTS_DIR);

        RecoveryJobManager recoveryJobManager = new RecoveryJobManagerStub();

        JobManager jobManager = new JobManager(sourceDirectory, dataManager, eventSender, new CleanStartupVerifierStub(null), recoveryJobManager);
        ContextHandler.highlightMarkup = false;
        jobManager.startJobs();
        Thread.sleep(5000);
    }

    /**
     * Note that in this test period there was bug in DAQAggregator. The output bandwidth in BUSummary object was not
     * flushed between snapshots. This results in HLTOutputBandwidthExtreme and HLTOutputBandwidthTooHigh being fired in
     * this test scenario.
     */
    @Test
    public void blackboxTest1() throws InterruptedException {

        String startDateString = "2016-11-30T12:19:20Z";
        String endDateString = "2016-11-30T12:27:30Z";


        Set<String> expectedConditions = new HashSet<>();
        expectedConditions.add("Dataflow stuck");
        expectedConditions.add("FED stuck");
        expectedConditions.add("Too high HLT output bandwidth");
        expectedConditions.add("FED problem");

        Set<String> expectedNotifications = new HashSet<>();
        expectedNotifications.add("TCDS State: Running");
        expectedNotifications.add("Started: FED stuck");
        expectedNotifications.add("Ended: FED stuck");

        Set<String> expectedConditionDescriptions = new HashSet<>();
        expectedConditionDescriptions.add("TTCP TIBTID of TRACKER subsystem is blocking triggers, it's in WARNING TTS state, The problem is caused by FED 101 in WARNING");
        expectedConditionDescriptions.add("Deadtime is ( last: 100%,  avg: 98.8%,  min: 79.2%,  max: 100%), the threshold is 5.0%");


        runForBlackboxTest(startDateString, endDateString);
        assertExpectedConditions(expectedConditions);
        assertExpectedNotifications(expectedNotifications);
        assertExpectedConditionDescriptions(expectedConditionDescriptions);
    }


    @Test
    public void blackboxTest2() throws InterruptedException {

        String startDateString = "2017-06-12T09:15:00Z";
        String endDateString = "2017-06-12T09:45:00Z";


        Set<String> expectedConditions = new HashSet<>();
        expectedConditions.add("Continuous fixing-soft-error");
        expectedConditions.add("FixingSoftError");
        expectedConditions.add("Running");
        expectedConditions.add("Run ongoing");
         //expectedConditions.add("Out of sequence data received");

        Set<String> expectedNotifications = new HashSet<>();
        expectedNotifications.add("Started: Continuous fixing-soft-error");
        expectedNotifications.add("Ended: Continuous fixing-soft-error");
        expectedNotifications.add("Level Zero State: FixingSoftError");
        expectedNotifications.add("Level Zero State: Running");

        Set<String> expectedConditionDescriptions = new HashSet<>();
        expectedConditionDescriptions.add("Level zero in FixingSoftError more than 3 times in past 10 min. This is caused by subsystem(s) [TRACKER 4 time(s)]");
        expectedConditionDescriptions.add("Partition TIBTID in TRACKER subsystem is in OUT_OF_SYNC TTS state. It's blocking triggers.");

        runForBlackboxTest(startDateString, endDateString);
        assertExpectedConditions(expectedConditions);
        assertExpectedNotifications(expectedNotifications);
        assertExpectedConditionDescriptions(expectedConditionDescriptions);
    }

    @Test
    @Ignore
    public void blackboxTest3() throws InterruptedException {

        String startDateString = "2017-06-02T21:15:00Z";
        String endDateString = "2017-06-02T21:30:00Z";

        Set<String> expectedConditions = new HashSet<>();
        expectedConditions.add("Lengthy fixing-soft-error");
        expectedConditions.add("Stuck after fixing-soft-error");
        expectedConditions.add("FixingSoftError");
        expectedConditions.add("Running");
        expectedConditions.add("Run ongoing");

        Set<String> expectedNotifications = new HashSet<>();
        expectedNotifications.add("Started: Stuck after fixing-soft-error");
        expectedNotifications.add("Ended: Stuck after fixing-soft-error");
        expectedNotifications.add("Started: Lengthy fixing-soft-error");
        expectedNotifications.add("Ended: Lengthy fixing-soft-error");
        expectedNotifications.add("Level Zero State: FixingSoftError");
        expectedNotifications.add("Level Zero State: Running");

        Set<String> expectedConditionDescriptions = new HashSet<>();
        expectedConditionDescriptions.add("Level zero is stuck after fixing soft error. This is caused by subsystem(s) ES");
        expectedConditionDescriptions.add("Level zero in FixingSoftError longer than 30 sec. This is caused by subsystem(s) ES");

        runForBlackboxTest(startDateString, endDateString);
        assertExpectedConditions(expectedConditions);
        assertExpectedNotifications(expectedNotifications);
        assertExpectedConditionDescriptions(expectedConditionDescriptions);
    }

    /**
     *
     * This case contains 38 conditions of Fedstuck due to fluctuating rate.
     */
    @Test
    public void blackboxTest4() throws InterruptedException {

        String startDateString = "2017-11-06T01:00:00Z";
        String endDateString = "2017-11-06T05:25:00Z";

        Set<String> expectedConditions = new HashSet<>();
        expectedConditions.add("Partition deadtime");
        expectedConditions.add("FED deadtime");
        expectedConditions.add("Dataflow stuck");
        expectedConditions.add("FED problem");
        expectedConditions.add("FED stuck");

        Set<String> expectedNotifications = new HashSet<>();
        expectedNotifications.add("Started: FED stuck");
        expectedNotifications.add("Ended: FED stuck");

        Set<String> expectedConditionDescriptions = new HashSet<>();
        expectedConditionDescriptions.add("TTCP CSC+ of CSC subsystem is blocking triggers, it's in WARNING TTS state, The problem is caused by FED 847 in WARNING");
        expectedConditionDescriptions.add("FED [847, 852-853] generates deadtime ( last: 12.2%,  avg: 51.5%,  min: 12.2%,  max: 100%), the threshold is 2.0%. There is no backpressure from DAQ on this FED. FED belongs to partition [CSC+, CSC-] in subsystem CSC");
        expectedConditionDescriptions.add("CSC/CSC+/847 is stuck in TTS state WARNING");
        expectedConditionDescriptions.add("TTCP CSC+ of CSC subsystem is blocking triggers, it's in WARNING TTS state, The problem is caused by FED 847 in WARNING");

        Set<RecoveryRequest> expectedRecoveryRequests = new HashSet<>();
        expectedRecoveryRequests.add(generateRecovery(3,"TTCP CSC+ of CSC subsystem is blocking triggers, it's in WARNING TTS state, The problem is caused by FED 847 in WARNING"));

        runForBlackboxTest(startDateString, endDateString);
        assertExpectedConditions(expectedConditions);
        assertExpectedNotifications(expectedNotifications);
        assertExpectedConditionDescriptions(expectedConditionDescriptions);

        /* TODO: should be exactly 39, but for some reason depending on test order it yields either 35 or 39 */
        assertExpectedRecoveryRequest(34, expectedRecoveryRequests);

    }

    /**
     * Test generated dominating entries
     */
    @Test

    public void blackboxTest5() throws InterruptedException {

        String startDateString = "2018-06-01T20:36:24.281Z";
        String endDateString = "2018-06-01T20:38:19.152Z";

        runForBlackboxTest(startDateString, endDateString);



        logger.info("Yielded dominating conditions:");
        conditionsYielded.stream().filter(c->c.getGroup() == ConditionGroup.DOMINATING).map(c-> c.getTitle() + " " + c.getStart() + " " + c.getEnd()).forEach(System.out::println);

        logger.info("All conditions:");
        conditionsYielded.stream().filter(c->c.isShow()).map(c-> c.getTitle() + " " + c.getGroup()+ " " + c.getStart() + " " + c.getEnd()).forEach(System.out::println);


        assertThat(conditionsYielded, hasItem(Matchers.<Condition>allOf(
                hasProperty("title", equalTo("Downtime")),
                hasProperty("group", equalTo(ConditionGroup.DOMINATING)),
                hasProperty("start", notNullValue()),
                hasProperty("end", notNullValue())
        )));


    }

    @Test
    public void blackboxTest6WrapperDemo() throws InterruptedException {
        blackboxTest6(true);
    }

    @Test
    public void blackboxTest6WrapperBatch() throws InterruptedException {
        blackboxTest6(false);
    }
    /**
     * Test merging of conditions
     *
     * 2018-06-01T16:58:47.734Z&end=2018-06-01T17:02:47.734Z
     */
    public void blackboxTest6(boolean demo) throws InterruptedException {

        String startDateString = "2018-06-01T16:58:47.734Z";
        String endDateString = "2018-06-01T17:02:47.734Z";

        runForBlackboxTest(startDateString, endDateString, demo);

        conditionsYielded.stream().filter(c->c.getGroup()== ConditionGroup.OTHER).map(c->c.getTitle() +": " + c.getDescription()).forEach(System.out::println);

        assertThat(conditionsYielded, hasItem(Matchers.<Condition>allOf(
                hasProperty("title", equalTo("Continuous fixing-soft-error")),
                hasProperty("description", equalTo("Level zero in FixingSoftError more than 3 times in past 10 min. This is caused by subsystem(s) [TRACKER 10 time(s)]")),
                hasProperty("group", equalTo(ConditionGroup.OTHER)),
                hasProperty("start", notNullValue()),
                hasProperty("end", notNullValue())
        )));




    }

    private RecoveryRequest generateRecovery(int steps, String problemDescription){
        RecoveryRequest rr = new RecoveryRequest();

        rr.setRecoverySteps(new ArrayList());
        for(int i = 0; i< steps; i++){
            rr.getRecoverySteps().add(new RecoveryStep());
        }

        rr.setProblemDescription(problemDescription);
        return rr;
    }
    /**
     * Clean before blackbox test scenarios
     */
    @Before
    public void clean() {

        logger.info("Cleaning before blackbox testing");
        conditionsYielded = new ArrayList<>();
        notifications = new ArrayList<>();
        recoveryRequestsYielded = new ArrayList<>();
    }



    public void runForBlackboxTest(String start, String end) throws InterruptedException {
        runForBlackboxTest(start, end, true);
    }

    /**
     * @param start start of the scenario timespan in ISO 8601 format, e.g. 2016-11-30T12:19:20Z
     * @param end   end of the scenario timespan in ISO 8601 format, e.g. 2016-11-30T12:19:20Z
     */
    public void runForBlackboxTest(String start, String end, boolean demo) throws InterruptedException {

        Date startDate = DatatypeConverter.parseDateTime(start).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();

        Application.initialize("src/test/resources/integration.properties");

        Application.get().getProp().setProperty("demo", String.valueOf(demo));
        HttpClient client = HttpClientBuilder.create().build();

        EventSender eventSender = new EventSenderStub();

        runOverTestPeriod(start, end, eventSender);


		/* Verify Conditions produced in DB */
        long durationThreshold = 0;
        boolean includeTinyEntriesMask = false;

        int retries = 0;
        int retriesLimit = 3;
        int lastSize = 0;
        Thread.sleep(1000);
        while(retries<retriesLimit) {

            int currentSize = (conditionsYielded!=null)? conditionsYielded.size(): 0;
            int increment = currentSize - lastSize;
            logger.info("Waiting for results: " + (retriesLimit-retries) + " more seconds");

            /* Things are processing here */
            if (conditionsYielded == null || increment > 0) { // 1 Because version condition is generated immediately
                retries = 0;
            }

            /* things most likely finished */
            else {
                logger.info("zero condition yielded");
                retries++;
            }
            lastSize = currentSize;
            conditionsYielded = Application.get().getPersistenceManager().getEntries(startDate, endDate, durationThreshold,
                    includeTinyEntriesMask, true);
            Thread.sleep(2000);

        }
        List<Point> rawResult = Application.get().getPersistenceManager().getRawData(startDate, endDate,
                DataResolution.Full);

        int visibleConditions = 0;
        for (Condition a : conditionsYielded) {
            if (a.isShow()) {
                visibleConditions++;
            }
        }

		/* Verify that the number of visible conditions was greater than 0 */
        Assert.assertTrue(0 < visibleConditions);

    }

    public void assertExpectedConditions(Collection<String> expectedConditions) {

        logger.info("Asserting expected condition titles. Following results (filtered) has been yielded in blackbox test scenario:");
        for (Condition c : conditionsYielded) {
            if (c.getLogicModule() == null) {
                logger.info("No LM condition: " + c.getTitle());
                continue;
            }
            if (c.getLogicModule().getLogicModule() instanceof ActionLogicModule || c.getLogicModule().getLogicModule() instanceof ContextLogicModule) {
                logger.info("    > " + c.getTitle());
            }
        }

        for (String conditionTitle : expectedConditions) {
            assertThat(conditionsYielded, hasItem(Matchers.<Condition>hasProperty("title", equalTo(conditionTitle))));
        }

    }

    public void assertExpectedConditionDescriptions(Collection<String> expectedConditionDescriptions) {

        logger.info("Asserting expected condition descriptions. Following results (filtered) has been yielded in blackbox test scenario:");
        for (Condition c : conditionsYielded) {
            if (c.getLogicModule() == null) {
                logger.info("No LM condition: " + c.getTitle() + ": " + c.getDescription());
                continue;
            }
            if (c.getLogicModule().getLogicModule() instanceof ActionLogicModule || c.getLogicModule().getLogicModule() instanceof ContextLogicModule) {
                logger.info("    > " + String.format("%1$30s", c.getTitle()) + "\t" + c.getDescription());
            }
        }

        for (String conditionDescription : expectedConditionDescriptions) {
            assertThat(conditionsYielded, hasItem(Matchers.<Condition>hasProperty("description", equalTo(conditionDescription))));
        }

    }

    public void assertExpectedNotifications(Collection<String> expectedNotifications) {

        notifications.stream().forEach(n->logger.info(n.toString()));

        assertThat(notifications.size(), greaterThan(0));

        for (String notificationTitle : expectedNotifications) {
            assertThat(notifications,
                    hasItem(Matchers.hasProperty("title", equalTo(notificationTitle))));
        }
    }

    public void assertExpectedRecoveryRequest(int totalNumberOfRecovoveryRequests, Collection<RecoveryRequest> expectedRecoveryRequests){

        recoveryRequestsYielded.stream().forEach(c->System.out.println("P " + c.getProblemId() + c.getCondition().getTitle() + c.getCondition().getStart()));
        //assertEquals(totalNumberOfRecovoveryRequests,  recoveryRequestsYielded.size());
        assertThat(recoveryRequestsYielded.size(), greaterThanOrEqualTo(totalNumberOfRecovoveryRequests));

        for (RecoveryRequest rr : expectedRecoveryRequests) {
            assertThat(recoveryRequestsYielded, hasItem(Matchers.<RecoveryRequest>hasProperty("problemDescription", equalTo(rr.getProblemDescription()))));
        }

    }

    class CleanStartupVerifierStub extends CleanStartupVerifier {

        public CleanStartupVerifierStub(PersistenceManager persistenceManager) {
            super(persistenceManager);
        }

        @Override
        public void ensureSafeStartupProcedure() {
        }
    }

    class RecoveryJobManagerStub extends RecoveryJobManager{


        public RecoveryJobManagerStub() {
            super(new ExpertControllerClientStub(""));
        }

        @Override
        public Long runRecoveryJob(List<RecoveryRequest> requests) {
            logger.info("Recovery job called: " + requests);
            recoveryRequestsYielded.addAll(requests);
            return 0L;
        }

    }

    class ExpertControllerClientStub extends ExpertControllerClient {

        public ExpertControllerClientStub(String mainUri) {
            super(mainUri);
        }

        @Override
        public RecoveryResponse sendRecoveryRequest(RecoveryRequest recoveryRequest) {

            logger.info("Sending recovery request: " + recoveryRequest);
            return null;
        }

        @Override
        public void sendConditionFinishedSignal(Long id) {
            logger.info("Sending signal");
        }
    }

    class EventSenderStub extends EventSender {

        public EventSenderStub(){
            super(null,null);
        }

        public EventSenderStub(HttpClient httpClient, String address) {
            super(httpClient, address);
        }

        @Override
        public int sendBatchEvents(List<ConditionEventResource> events) {
            notifications.addAll(events);
            return 0;
        }

        @Override
        public int sendEventsIndividually(List<ConditionEventResource> events) {
            notifications.addAll(events);
            return 0;
        }
    }
}
