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

	@Test
	public void test() throws InterruptedException {

		Application.initialize("src/test/resources/integration.properties");

		DataManager dataManager = new DataManager();

		HttpClient client = HttpClientBuilder.create().build();
		EventSender eventSender = Mockito
				.spy(new EventSender(client, Application.get().getProp(Setting.NM_API_CREATE)));

		String sourceDirectory = Application.get().getProp(Setting.SNAPSHOTS_DIR);
		JobManager jobManager = new JobManager(sourceDirectory, dataManager, eventSender);

		jobManager.startJobs();

		Thread.sleep(8000);

		/* Verify Conditions produced in DB */
		Date startDate = DatatypeConverter.parseDateTime("2016-11-30T11:00:20").getTime();
		Date endDate = DatatypeConverter.parseDateTime("2016-11-30T14:00:30Z").getTime();
		long durationThreshold = 0;
		boolean includeTinyEntriesMask = false;
		List<Condition> result = Application.get().getPersistenceManager().getEntries(startDate, endDate,
				durationThreshold, includeTinyEntriesMask);

		Assert.assertEquals(52, result.size());
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("No rate when expected"))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Out of sequence data received")))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Corrupted data received")))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Partition problem")))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Partition disconnected")))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("title", is("FED stuck"))));
		assertThat(result, hasItem(Matchers.<Condition> hasProperty("description", is(
				"TTCP TIBTID of TRACKER subsystem is blocking trigger, it's in WARNING TTS state, The problem is caused by FED 101 in WARNING"))));
		assertThat(result, not(hasItem(Matchers.<Condition> hasProperty("title", is("Backpressure detected")))));

		/* Verify Raw data produced in DB */
		List<Point> rawResult = Application.get().getPersistenceManager().getRawData(startDate, endDate,
				DataResolution.Full);
		Assert.assertEquals(482, rawResult.size());

		/* Verify generation of notifaications */
		Mockito.verify(eventSender, Mockito.times(1)).sendBatchEvents(Mockito.anyList());
		Mockito.verify(eventSender).sendBatchEvents((List) argThat(IsCollectionWithSize.hasSize(44)));
	}

}
