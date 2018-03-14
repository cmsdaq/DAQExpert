package rcms.utilities.daqexpert.processing;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.*;
import org.mockito.Mockito;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.processing.ConditionProducer;
import rcms.utilities.daqexpert.segmentation.DataResolution;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.argThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class JobManagerIT {

    private static final Logger logger = Logger.getLogger(JobManagerIT.class);
    private List<Condition> conditionsYielded;
    private EventSender eventSender;

    @BeforeClass
    public static void prepareNMStub() {
        MockServerClient mockServer = startClientAndServer(18081);
        mockServer.when(request().withMethod("POST").withPath("/nm/rest/events/"), exactly(1))
                .respond(
                        response().withStatusCode(201)
                                .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withDelay(new Delay(SECONDS, 1)));

        ConditionProducer.enableMarkup = false;
    }


    private void runOverTestPeriod(String startDateString, String endDateString, EventSender eventSender)
            throws InterruptedException {

        Application.get().getProp().setProperty(Setting.PROCESSING_START_DATETIME.getKey(), startDateString);
        Application.get().getProp().setProperty(Setting.PROCESSING_END_DATETIME.getKey(), endDateString);
        DataManager dataManager = new DataManager();
        String sourceDirectory = Application.get().getProp(Setting.SNAPSHOTS_DIR);
        JobManager jobManager = new JobManager(sourceDirectory, dataManager, eventSender, new CleanStartupVerifierStub(null));
        jobManager.startJobs();
        Thread.sleep(1000);
    }

    /**
     * Note that in this test period there was bug in DAQAggregator.
     * The output bandwidth in BUSummary object was not flushed between snapshots.
     * This results in HLTOutputBandwidthExtreme and HLTOutputBandwidthTooHigh being fired in this test scenario.
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
        expectedConditionDescriptions.add("TTCP TIBTID of TRACKER subsystem is blocking trigger, it's in WARNING TTS state, The problem is caused by FED 101 in WARNING");
        expectedConditionDescriptions.add("Deadtime is ( last: 100%,  avg: 98.8%,  min: 79.2%,  max: 100%), the threshold is 5.0%");


        runForBlackboxTest(startDateString, endDateString);
        assertExpectedConditions(expectedConditions);
        assertExpectedNotifications(expectedNotifications);
        assertExpectedConditionDescriptions(expectedConditionDescriptions);
    }

    @Test
    @Ignore
    public void test2() throws InterruptedException {

        String startDateString = "2017-06-12T09:15:00Z";
        String endDateString = "2017-06-12T09:45:00Z";

        Application.initialize("src/test/resources/integration.properties");
        HttpClient client = HttpClientBuilder.create().build();

        EventSender eventSender = Mockito
                .spy(new EventSender(client, Application.get().getProp(Setting.NM_API_CREATE)));

        runOverTestPeriod(startDateString, endDateString, eventSender);

        Date startDate = DatatypeConverter.parseDateTime(startDateString).getTime();
        Date endDate = DatatypeConverter.parseDateTime(endDateString).getTime();

		/* Verify Conditions produced in DB */
        long durationThreshold = 0;
        boolean includeTinyEntriesMask = false;
        List<Condition> result = null;

        int retries = 10;
        int expectedResult = 63;
        for (int i = 0; i < retries; i++) {
            if (result == null || result.size() != expectedResult) {
                Thread.sleep(1000);
                result = Application.get().getPersistenceManager().getEntries(startDate, endDate, durationThreshold,
                        includeTinyEntriesMask);
            }

        }
        Assert.assertEquals(expectedResult, result.size());

        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Continous fixing-soft-error"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("FixingSoftError"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Running"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Run ongoing"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("description", is(
                "Level zero in FixingSoftError more than 3 times in past 10 min. This is caused by subsystem(s) [\"ES 1 time(s)\",\"TRACKER 4 time(s)\"]"))));

		/* Verify Raw data produced in DB */
        List<Point> rawResult = Application.get().getPersistenceManager().getRawData(startDate, endDate,
                DataResolution.Full);
        Assert.assertEquals(208, rawResult.size());

		/* Verify generation of notifaications */
        Mockito.verify(eventSender, Mockito.times(1)).sendBatchEvents(Mockito.anyList());

        // verify 42 events if mature-event-collector is used
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(IsCollectionWithSize.hasSize(42)));

        // verify 43 events if regular event-collector is used
        // Mockito.verify(eventSender).sendBatchEvents((List)
        // argThat(IsCollectionWithSize.hasSize(43)));

        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Started: Continous fixing-soft-error")))));
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Ended: Continous fixing-soft-error")))));
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Level Zero State: FixingSoftError")))));
        Mockito.verify(eventSender).sendBatchEvents(
                (List) argThat(hasItem(Matchers.<Condition>hasProperty("title", is("Level Zero State: Running")))));

    }

    @Test
    @Ignore
    public void test3() throws InterruptedException {

        String startDateString = "2017-06-02T21:15:00Z";
        String endDateString = "2017-06-02T21:30:00Z";

        Application.initialize("src/test/resources/integration.properties");
        HttpClient client = HttpClientBuilder.create().build();

        EventSender eventSender = Mockito
                .spy(new EventSender(client, Application.get().getProp(Setting.NM_API_CREATE)));

        runOverTestPeriod(startDateString, endDateString, eventSender);

        Date startDate = DatatypeConverter.parseDateTime(startDateString).getTime();
        Date endDate = DatatypeConverter.parseDateTime(endDateString).getTime();

		/* Verify Conditions produced in DB */
        long durationThreshold = 0;
        boolean includeTinyEntriesMask = false;
        List<Condition> result = null;

        int retries = 10;
        int expectedResult = 45;
        for (int i = 0; i < retries; i++) {
            if (result == null || result.size() != expectedResult) {
                Thread.sleep(1000);
                result = Application.get().getPersistenceManager().getEntries(startDate, endDate, durationThreshold,
                        includeTinyEntriesMask);
            }

        }
        Assert.assertEquals(expectedResult, result.size());

        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Stuck after fixing-soft-error"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("description",
                is("Level zero is stuck after fixing soft error. This is caused by subsystem(s) ES"))));

        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Lengthy fixing-soft-error"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("description",
                is("Level zero in FixingSoftError longer than 30 sec. This is caused by subsystem(s) ES"))));

        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("FixingSoftError"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Running"))));
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("title", is("Run ongoing"))));

		/* Verify Raw data produced in DB */
        List<Point> rawResult = Application.get().getPersistenceManager().getRawData(startDate, endDate,
                DataResolution.Full);
        Assert.assertEquals(634, rawResult.size());

		/* Verify generation of notifaications */
        Mockito.verify(eventSender, Mockito.times(1)).sendBatchEvents(Mockito.anyList());

        // verify 41 events if mature-event-collector is used
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(IsCollectionWithSize.hasSize(41)));

        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Started: Stuck after fixing-soft-error")))));
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Ended: Stuck after fixing-soft-error")))));

        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Started: Lengthy fixing-soft-error")))));
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Ended: Lengthy fixing-soft-error")))));

        Mockito.verify(eventSender).sendBatchEvents((List) argThat(
                hasItem(Matchers.<Condition>hasProperty("title", is("Level Zero State: FixingSoftError")))));
        Mockito.verify(eventSender).sendBatchEvents(
                (List) argThat(hasItem(Matchers.<Condition>hasProperty("title", is("Level Zero State: Running")))));

    }



    /**
     * Clean before blackbox test scenarios
     */
    @Before
    public void clean() {

        logger.info("Cleaning before blackbox testing");
        eventSender = null;
        conditionsYielded = null;
    }


    /**
     * @param start start of the scenario timespan in ISO 8601 format, e.g. 2016-11-30T12:19:20Z
     * @param end   end of the scenario timespan in ISO 8601 format, e.g. 2016-11-30T12:19:20Z
     */
    public void runForBlackboxTest(String start, String end) throws InterruptedException {

        Date startDate = DatatypeConverter.parseDateTime(start).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();

        Application.initialize("src/test/resources/integration.properties");
        HttpClient client = HttpClientBuilder.create().build();

        eventSender = Mockito
                .spy(new EventSender(client, Application.get().getProp(Setting.NM_API_CREATE)));

        runOverTestPeriod(start, end, eventSender);


		/* Verify Conditions produced in DB */
        long durationThreshold = 0;
        boolean includeTinyEntriesMask = false;

        int retries = 15;
        int last = 0;
        Thread.sleep(1000);
        for (int i = 0; i < retries; i++) {
            logger.info("Waiting for results: " + (i + 1));
            if (conditionsYielded == null || conditionsYielded.size() == 0 || conditionsYielded.size() != last) {
                Thread.sleep(1000);
                last = conditionsYielded != null ? conditionsYielded.size() : 0;
                conditionsYielded = Application.get().getPersistenceManager().getEntries(startDate, endDate, durationThreshold,
                        includeTinyEntriesMask);
            }

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
                logger.info("    > " + String.format("%1$30s", c.getTitle())+ "\t" + c.getDescription());
            }
        }

        for (String conditionDescription : expectedConditionDescriptions) {
            assertThat(conditionsYielded, hasItem(Matchers.<Condition>hasProperty("description", equalTo(conditionDescription))));
        }

    }

    public void assertExpectedNotifications(Collection<String> expectedNotifications) {
        /* Verify that all notifications were sent in one batch */
        Mockito.verify(eventSender, Mockito.times(1)).sendBatchEvents(Mockito.anyList());

		/* Verify that the number of notifications generated was greater than 0 */
        Mockito.verify(eventSender).sendBatchEvents((List) argThat(IsCollectionWithSize.hasSize(greaterThan(0))));


        for (String notificationTitle : expectedNotifications) {
            Mockito.verify(eventSender).sendBatchEvents(
                    (List) argThat(hasItem(Matchers.<Condition>hasProperty("title", equalTo(notificationTitle)))));
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
}
