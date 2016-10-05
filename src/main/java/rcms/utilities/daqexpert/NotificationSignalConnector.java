package rcms.utilities.daqexpert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class NotificationSignalConnector {

	private static Logger logger = Logger.getLogger(NotificationSignalConnector.class);

	public int sendSignal(String address, String content) {
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
