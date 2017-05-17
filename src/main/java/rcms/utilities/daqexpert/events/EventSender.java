package rcms.utilities.daqexpert.events;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;

/**
 * Sends events to Notification Manager
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class EventSender {

	private static Logger logger = Logger.getLogger(EventSender.class);
	private final ObjectMapper objectMapper;

	private final String address;
	private final HttpClient client;

	public EventSender(HttpClient httpClient, String address) {

		client = httpClient;
		this.address = address;
		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

	}

	public int sendBatchEvents(List<ConditionEventResource> events) {

		for (ConditionEventResource event : events) {
			logger.debug("Sending: " + event.getConditionId() + " " + event.getEventType() + " :   " + event.getTitle());
		}
		try {
			sendEvents(events);
			return events.size();
		} catch (ExpertException e) {
			return 0;
		}
	}

	public int sendEventsIndividually(List<ConditionEventResource> events) {

		int success = 0;
		int failed = 0;
		String exceptionSample = null;
		for (ConditionEventResource event : events) {

			try {
				sendEvent(event);
				success++;
			} catch (ExpertException e) {
				failed++;
				exceptionSample = e.getMessage();
			}
		}
		if (failed != 0) {
			logger.warn(failed + " events failed to send, " + success + " successful, one of the failures: "
					+ exceptionSample);
		} else if (success != 0) {
			logger.info("All " + success + " events successfully sent to nm");
		}
		return success;
	}

	private void sendEvents(List<ConditionEventResource> events) {
		try {

			String input = objectMapper.writeValueAsString(events);

			logger.debug("Request: " + input);
			sendJson(input);
		} catch (JsonProcessingException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception converting event to json: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception converting event to json, endcoding: " + e.getMessage());
		} catch (ClientProtocolException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception sending event to NM, protocol: " + e.getMessage());
		} catch (IOException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception sending event to NM, IO: " + e.getMessage());
		}

	}

	private void sendEvent(ConditionEventResource event) {
		try {

			String input = objectMapper.writeValueAsString(event);

			logger.debug("Request: " + input);
			sendJson(input);
		} catch (JsonProcessingException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception converting event to json: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception converting event to json, endcoding: " + e.getMessage());
		} catch (ClientProtocolException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception sending event to NM, protocol: " + e.getMessage());
		} catch (IOException e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception sending event to NM, IO: " + e.getMessage());
		}

	}

	private void sendJson(String event) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(address);
		post.addHeader("content-type", "application/json");

		StringEntity entity = new StringEntity(event);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
			logger.info(response);
			throw new ExpertException(ExpertExceptionCode.ExpertProblem,
					"Exception sending event to NM, status differend than 201: "
							+ response.getStatusLine().getStatusCode());
		}
	}

}
