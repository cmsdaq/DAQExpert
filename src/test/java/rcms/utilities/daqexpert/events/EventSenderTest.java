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
import org.mockito.internal.configuration.injection.MockInjection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class EventSenderTest {

	@Test
	public void connectionRefusedTest() {
		List<ConditionEventResource> list = new ArrayList<>();
		RestTemplate restTemplate = new RestTemplate();
		EventSender sut = new EventSender(restTemplate, "http://localhost:80/a/b/c");
		list.add(generate());
		int result = sut.sendEventsIndividually(list);
		Assert.assertEquals("Cannot send", 0, result);
	}

	@Test
	public void successfulTest() throws ClientProtocolException, IOException {
		// given:
		RestTemplate restTemplate = mock(RestTemplate.class);
		ResponseEntity responseEntity = mock(ResponseEntity.class);

		// and:
		when(responseEntity.getStatusCode()).thenReturn(HttpStatus.CREATED);
		when(restTemplate.postForEntity(Mockito.any(String.class), Mockito.any(), Mockito.any())).thenReturn(responseEntity);

		List<ConditionEventResource> list = new ArrayList<>();
		EventSender sut = new EventSender(restTemplate, "http://localhost:80/a/b/c");
		list.add(generate());
		int result = sut.sendEventsIndividually(list);
		Assert.assertEquals("One sent", 1, result);
	}

	@Test
	public void wrongStatusReturned() throws ClientProtocolException, IOException {
		// given:
		RestTemplate restTemplate = mock(RestTemplate.class);
		ResponseEntity responseEntity = mock(ResponseEntity.class);

		// and:
		when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		when(restTemplate.postForEntity(Mockito.any(String.class), Mockito.any(), Mockito.any())).thenReturn(responseEntity);

		List<ConditionEventResource> list = new ArrayList<>();
		EventSender sut = new EventSender(restTemplate, "http://localhost:80/a/b/c");
		list.add(generate());
		int result = sut.sendEventsIndividually(list);
		Assert.assertEquals("One sent", 0, result);
	}

	private ConditionEventResource generate() {
		ConditionEventResource cer = new ConditionEventResource();
		return cer;
	}

}
