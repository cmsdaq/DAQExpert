package rcms.utilities.daqexpert.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventSender {

	private static Logger logger = Logger.getLogger(EventSender.class);
	private final ObjectMapper objectMapper;

	private final String address;

	public EventSender(String address) {
		this.address = address;

		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

	}

	public boolean send(EventToSend event) {
		try {

			if (event.getMessage() == null) {
				logger.warn("Message empty for event: " + event.getTitle() + ", replacing with empty string");
				event.setMessage("");
			}

			String input = objectMapper.writeValueAsString(event);

			logger.debug("Request: " + input);
			return sendEvent(input);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	private boolean sendEvent(String input) {
		try {

			URL url = new URL(address);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				logger.error("Failed: HTTP error code : " + conn.getResponseCode());
				logger.error(conn.getResponseMessage());
				// throw new RuntimeException("Failed : HTTP error code : " +
				// conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			logger.trace("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				logger.trace(output);
			}

			conn.disconnect();

			return true;

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return false;
	}
}
