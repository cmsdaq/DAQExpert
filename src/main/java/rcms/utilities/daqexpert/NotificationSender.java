package rcms.utilities.daqexpert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.ContextCollector;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EntryState;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;

public class NotificationSender {

	private static final Logger logger = Logger.getLogger(NotificationSender.class);

	private ObjectMapper objectMapper;

	private String createAPIAddress;

	private String finishAPIAddress;

	public NotificationSender() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		createAPIAddress = Application.get().getProp().getProperty(Application.NM_API_CREATE);
		finishAPIAddress = Application.get().getProp().getProperty(Application.NM_API_CLOSE);
	}

	public void rtSend(Entry entry) {

		if ("critical".equals(entry.getClassName())) {
			EntryState state = entry.getState();
			switch (state) {
			case NEW:
				// imediately in 2nd snapshot becomes stable
				// may check date here
				entry.setState(EntryState.NEW_STABLE);
				break;
			case NEW_STABLE:
				entry.setState(EntryState.STARTED);
				// send started notification
				logger.info("Send entry notification, id: " + entry.getId());
				sendEntry(entry);
				sentIds.add(entry.getId());
				break;
			case STARTED:
				// send update
				if (entry.hasChanged())
					logger.info("Send entry update, id: " + entry.getId());
				break;
			case FINISHED:
				// send finished
				if (sentIds.contains(entry.getId())) {
					logger.info("Send entry finish, id: " + entry);
					sendFinish(entry);
					sentIds.remove(entry.getId());
				}
				break;
			default:
				break;
			}
		}
	}

	private Set<Long> sentIds = new HashSet<>();

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
			// entry.setContent("Event from java " + i);
			entries.add(entry);

			notificationSender.rtSend(entry);
		}

	}

	private int sendEntry(Entry event) {

		if (event.getEventFinder() instanceof ExtendedCondition) {

			ExtendedCondition finder = (ExtendedCondition) event.getEventFinder();
			Notification notification = new Notification();
			notification.setDate(event.getStart());

			String message = finder.getDescription();

			logger.info("Now working on: " + message);
			logger.info("Now working on: " + event);

			if (finder instanceof ExtendedCondition) {
				ContextCollector context = ((ExtendedCondition) finder).getContext();
				message = context.getMessageWithContext(message);
			}

			notification.setMessage(message);
			notification.setAction(finder.getAction());
			notification.setType_id(1);
			notification.setId(event.getId());

			logger.info("To be sent: " + notification);

			String notificationString;
			try {
				notificationString = objectMapper.writeValueAsString(notification);
				return send(createAPIAddress, notificationString);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return 0;

	}

	private void sendFinish(Entry entry) {
		FinishNotification finishNotification = new FinishNotification();
		finishNotification.setId(entry.getId());
		finishNotification.setDate(entry.getEnd());
		try {
			String notificationAsString = objectMapper.writeValueAsString(finishNotification);
			send(finishAPIAddress, notificationAsString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private int send(String address, String content) {
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(address);

			logger.debug("sending content: " + content);
			StringEntity input = new StringEntity(content);

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
		} catch (IOException e) {
			return 0;
		}
	}

}
