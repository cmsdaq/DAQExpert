package rcms.utilities.daqaggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.reasoning.base.Entry;

public class NotificationSender {

	private static final Logger logger = Logger.getLogger(NotificationSender.class);

	private ObjectMapper objectMapper;

	public NotificationSender() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	public void send(List<Entry> list) {
		int sent = 0;
		for (Entry event : list) {
			if ("critical".equals(event.getClassName())) {
				logger.debug("Sending notification: " + event.getContent());

				Notification notification = new Notification();
				notification.setDate(event.getStart());
				String message = "Event: " + event.getContent();
				message = message + ", date: " + event.getStart();
				try {
					message = message + ", raport: " + objectMapper.writeValueAsString(event.getEventRaport());
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				notification.setMessage(message);
				notification.setType_id(1);
				try {
					sent++;
					sendEntry(notification);
				} catch (RuntimeException e) {
					logger.error(e);
				}
			}
		}
		if (sent != 0)
			logger.info(sent + " notifications sent");
	}

	public static void main(String[] args) {
		NotificationSender notificationSender = new NotificationSender();

		Calendar cal = Calendar.getInstance(); // creates calendar
		cal.setTime(new Date()); // sets calendar time/date
		cal.add(Calendar.MINUTE, 1); // adds one hour
		cal.getTime();

		List<Entry> entries = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			cal.add(Calendar.MINUTE, 5);
			Entry entry = new Entry();
			entry.setClassName("critical");
			entry.setStart(cal.getTime());
			cal.add(Calendar.MINUTE, 1);
			entry.setEnd(cal.getTime());
			entry.setContent("Event from java " + i);
			entries.add(entry);
		}

		notificationSender.send(entries);
	}

	public void sendEntry(Notification notification) {

		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			// HttpPost postRequest = new
			// HttpPost("https://dvbu-pcintelsz.cern.ch/nm-1.0.ALPHA/rest/events/");
			HttpPost postRequest = new HttpPost("http://localhost:8081/nm-1.1/rest/events/");

			String notificationString = objectMapper.writeValueAsString(notification);

			logger.info("sending notification: " + notificationString);
			StringEntity input = new StringEntity(notificationString);

			input.setContentType("application/json");
			postRequest.setEntity(input);

			HttpResponse response = httpClient.execute(postRequest);
			// TODO:should be either 201 or 200
			if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {

				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			logger.debug("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				logger.debug(output);
			}

			httpClient.getConnectionManager().shutdown();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

}
