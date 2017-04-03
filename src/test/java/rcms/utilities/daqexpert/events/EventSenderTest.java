package rcms.utilities.daqexpert.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class EventSenderTest {

	@Test
	public void connectionRefusedTest() {
		List<ConditionEventResource> list = new ArrayList<>();
		HttpClient client = HttpClientBuilder.create().build();
		EventSender sut = new EventSender(client, "http://localhost:80/a/b/c");
		list.add(generate());
		int result = sut.sendEventsIndividually(list);
		Assert.assertEquals("Cannot send", 0, result);
	}

	@Test
	public void successfulTest() throws ClientProtocolException, IOException {
		// given:
		HttpClient httpClient = mock(HttpClient.class);
		HttpResponse httpResponse = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);

		// and:
		when(statusLine.getStatusCode()).thenReturn(201);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

		List<ConditionEventResource> list = new ArrayList<>();
		EventSender sut = new EventSender(httpClient, "http://localhost:80/a/b/c");
		list.add(generate());
		int result = sut.sendEventsIndividually(list);
		Assert.assertEquals("One sent", 1, result);
	}

	@Test
	public void wrongStatusReturned() throws ClientProtocolException, IOException {
		// given:
		HttpClient httpClient = mock(HttpClient.class);
		HttpResponse httpResponse = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);

		// and:
		when(statusLine.getStatusCode()).thenReturn(200);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

		List<ConditionEventResource> list = new ArrayList<>();
		EventSender sut = new EventSender(httpClient, "http://localhost:80/a/b/c");
		list.add(generate());
		int result = sut.sendEventsIndividually(list);
		Assert.assertEquals("One sent", 0, result);
	}

	private ConditionEventResource generate() {
		ConditionEventResource cer = new ConditionEventResource();
		return cer;
	}

}
