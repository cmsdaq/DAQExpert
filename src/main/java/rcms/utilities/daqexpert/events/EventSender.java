package rcms.utilities.daqexpert.events;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
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

	RestTemplate restTemplate;

	public EventSender( RestTemplate restTemplate, String address) {

		this.address = address;
		this.restTemplate = restTemplate;
		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

	}

	public int sendBatchEvents(List<ConditionEventResource> events) {

		for (ConditionEventResource event : events) {
			logger.debug("Sending: " + event.getConditionId() + " " + event.getEventType() + " :   " + event.getTitle());
		}
		try {
			boolean successful = sendEvents(events);
			if(successful){
				return events.size();
			} else {
				return 0;
			}
		} catch (ExpertException e) {
			logger.warn("Problem sending events to NM: " + e.getMessage());
			return 0;
		}
	}

	public int sendEventsIndividually(List<ConditionEventResource> events) {

		int success = 0;
		int failed = 0;
		String exceptionSample = null;
		for (ConditionEventResource event : events) {

			try {
				boolean successful = sendEvents(Stream.of(event).collect(Collectors.toList()));
				if(successful)
					success++;
				else
					failed++;
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

	private boolean sendEvents(List<ConditionEventResource> events) {

		try {
			logger.info("Sending events: "  + events);
			ResponseEntity<Void> response = restTemplate.postForEntity(address, events, Void.class);

			if(response.getStatusCode() != HttpStatus.CREATED){
				logger.warn("Problem sending events to NM");
				return false;
			}
			return true;

		}catch(RestClientException e){
			logger.error("Requests to " + address + " failed");
			logger.error(e);
			return false;
		}

	}

}
