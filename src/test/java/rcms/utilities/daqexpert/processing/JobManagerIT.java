package rcms.utilities.daqexpert.processing;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.segmentation.DataResolution;

public class JobManagerIT {

	@BeforeClass
	public static void prepareNMStub() {
		MockServerClient mockServer = startClientAndServer(18081);
		mockServer.when(request().withMethod("POST").withPath("/nm/rest/events/"), exactly(1))
				.respond(
						response().withStatusCode(201)
								.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
										new Header("Cache-Control", "public, max-age=86400"))
								.withDelay(new Delay(SECONDS, 1)));

	}

	private void runOverTestPeriod(String startDateString, String endDateString, EventSender eventSender)
			throws InterruptedException {

		Application.get().getProp().setProperty("processing.start", startDateString);
		Application.get().getProp().setProperty("processing.end", endDateString);
		DataManager dataManager = new DataManager();
		String sourceDirectory = Application.get().getProp(Setting.SNAPSHOTS_DIR);
		JobManager jobManager = new JobManager(sourceDirectory, dataManager, eventSender);
		jobManager.startJobs();
		Thread.sleep(1000);
	}

    /**
     * Note that in this test period there was bug in DAQAggregator.
     * The output bandwidth in BUSummary object was not flushed between snapshots.
     * This results in HLTOutputBandwidthExtreme and HLTOutputBandwidthTooHigh being fired in this test scenario.
     */
	@Test
	public void test() throws InterruptedException {

		String startDateString = "2016-11-30T11:00:20Z";
		String endDateString = "2016-11-30T14:00:30Z";

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

		int retries = 15;
		int expectedResult = 61;
		for (int i = 0; i < retries; i++) {
			if (result == null || result.size() != expectedResult) {
				Thread.sleep(1000);
				result = Application.get().getPersistenceManager().getEntries(startDate, endDate, durationThreshold,
						includeTinyEntriesMask);
			}

		}
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("No rate when expected"))));
		assertThat(result,
				not(hasItem(Matchers.<Condition> hasProperty("title", is("Out of sequence data received")))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Corrupted data received")))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Partition problem")))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Partition disconnected")))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("FED stuck"))));

		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Too high HLT output bandwidth"))));

		assertThat(result, hasItem(Matchers.<Condition> hasProperty("description", is(
				"TTCP TIBTID of TRACKER subsystem is blocking trigger, it's in WARNING TTS state, The problem is caused by FED 101 in WARNING"))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Backpressure detected")))));

        System.out.println(result.toString());
        assertThat(result, hasItem(Matchers.<Condition>hasProperty("description", is(
                "Deadtime is <strong>(<sub><sup> last: </sup></sub>100%, <sub><sup> avg: </sup></sub>98.8%, <sub><sup> min: </sup></sub>79.2%, <sub><sup> max: </sup></sub>100%)</strong>, the threshold is 5.0%"))));
		assertThat(result, hasItem(Matchers.<Condition>hasProperty("description", is(
				"Deadtime is <strong>6.3%</strong>, the threshold is 5.0%"))));


		Assert.assertEquals(expectedResult, result.size());

		/* Verify Raw data produced in DB */
		List<Point> rawResult = Application.get().getPersistenceManager().getRawData(startDate, endDate,
				DataResolution.Full);
		Assert.assertEquals(482, rawResult.size());

		/* Verify generation of notifaications */
		Mockito.verify(eventSender, Mockito.times(1)).sendBatchEvents(Mockito.anyList());

		// verify 49 events if mature-event-collector is used
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(IsCollectionWithSize.hasSize(57)));

		Mockito.verify(eventSender).sendBatchEvents(
				(List) argThat(hasItem(Matchers.<Condition> hasProperty("title", is("Started: FED stuck")))));
		Mockito.verify(eventSender).sendBatchEvents(
				(List) argThat(hasItem(Matchers.<Condition> hasProperty("title", is("Ended: FED stuck")))));
		Mockito.verify(eventSender).sendBatchEvents(
				(List) argThat(hasItem(Matchers.<Condition> hasProperty("title", is("TCDS State: Running")))));

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

		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Continous fixing-soft-error"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("FixingSoftError"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Running"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Run ongoing"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("description", is(
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
				hasItem(Matchers.<Condition> hasProperty("title", is("Started: Continous fixing-soft-error")))));
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Ended: Continous fixing-soft-error")))));
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Level Zero State: FixingSoftError")))));
		Mockito.verify(eventSender).sendBatchEvents(
				(List) argThat(hasItem(Matchers.<Condition> hasProperty("title", is("Level Zero State: Running")))));

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

		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Stuck after fixing-soft-error"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("description",
				is("Level zero is stuck after fixing soft error. This is caused by subsystem(s) ES"))));

		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Lengthy fixing-soft-error"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("description",
				is("Level zero in FixingSoftError longer than 30 sec. This is caused by subsystem(s) ES"))));

		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("FixingSoftError"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Running"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("Run ongoing"))));

		/* Verify Raw data produced in DB */
		List<Point> rawResult = Application.get().getPersistenceManager().getRawData(startDate, endDate,
				DataResolution.Full);
		Assert.assertEquals(634, rawResult.size());

		/* Verify generation of notifaications */
		Mockito.verify(eventSender, Mockito.times(1)).sendBatchEvents(Mockito.anyList());

		// verify 41 events if mature-event-collector is used
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(IsCollectionWithSize.hasSize(41)));

		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Started: Stuck after fixing-soft-error")))));
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Ended: Stuck after fixing-soft-error")))));

		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Started: Lengthy fixing-soft-error")))));
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Ended: Lengthy fixing-soft-error")))));

		Mockito.verify(eventSender).sendBatchEvents((List) argThat(
				hasItem(Matchers.<Condition> hasProperty("title", is("Level Zero State: FixingSoftError")))));
		Mockito.verify(eventSender).sendBatchEvents(
				(List) argThat(hasItem(Matchers.<Condition> hasProperty("title", is("Level Zero State: Running")))));

	}

}
