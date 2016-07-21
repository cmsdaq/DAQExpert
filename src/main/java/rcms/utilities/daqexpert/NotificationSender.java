package rcms.utilities.daqexpert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

import rcms.utilities.daqaggregator.Notification;
import rcms.utilities.daqexpert.reasoning.base.Entry;

public class NotificationSender {

	private static final Logger logger = Logger.getLogger(NotificationSender.class);

	private ObjectMapper objectMapper;

	private String destinationAddress;

	public NotificationSender() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		destinationAddress = Application.get().getProp().getProperty(Application.NM_API);
	}

	public void send(List<Entry> list) {
		int successfullySent = 0;
		int unsuccessfullySent = 0;
		List<Integer> errors = new ArrayList<>();
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
				int httpResponseCode = sendEntry(notification, destinationAddress);

				if (httpResponseCode == 201)
					successfullySent++;
				else {
					unsuccessfullySent++;
					errors.add(httpResponseCode);
				}

			}
		}
		if (successfullySent != 0)
			logger.info(successfullySent + " notifications sent to:" + destinationAddress);
		if (unsuccessfullySent != 0){
			logger.error("Failed to send " + unsuccessfullySent + " notifications to: " + destinationAddress);
			logger.error("Notification requests results: " + errors);
		}

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

	public int sendEntry(Notification notification, String destinationAddress) {

		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(destinationAddress);

			String notificationString;
			notificationString = objectMapper.writeValueAsString(notification);

			logger.debug("sending notification: " + notificationString);
			StringEntity input = new StringEntity(notificationString);

			input.setContentType("application/json");
			postRequest.setEntity(input);

			HttpResponse response = httpClient.execute(postRequest);
			// TODO:should be either 201 or 200
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 201) {
				return statusCode;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			logger.debug("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				logger.debug(output);
			}

			httpClient.getConnectionManager().shutdown();
			return statusCode;
		} catch (JsonProcessingException e) {
			return 0;
		} catch (UnsupportedEncodingException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}
	}

}
