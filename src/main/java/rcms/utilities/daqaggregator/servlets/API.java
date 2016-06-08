package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.TaskManager;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;

/**
 * Event occurrences servlet API, used for async requests in autoupdate mode.
 * 
 * This API is used by main servlet for event occurrences view.
 * 
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class API extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(API.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");

		logger.info("Getting data from : " + startRange + " to " + endRange);

		List<Object> fedBuilderSummaries = new ArrayList<>();
		List<String> dates = new ArrayList<>();

		// random data
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(tz);

		for (int i = 0; i < 0; i++) {

			c.add(Calendar.MINUTE, 1);
			Date dt = c.getTime();
			FEDBuilderSummary fedBuilderSummary = new FEDBuilderSummary();
			fedBuilderSummary.setRate(1000 + (i * i) * 100);
			fedBuilderSummary.setThroughput(400 + (i) * 10);
			fedBuilderSummary.setSumRequests(200 - (i));
			fedBuilderSummaries.add(fedBuilderSummary);
			dates.add(df.format(dt));
		}
		

		/* iterate over objects in given range */
		for (DAQ daq : TaskManager.get().buf) {
			if (daq.getLastUpdate() >= Long.parseLong(startRange) && daq.getLastUpdate() <= Long.parseLong(endRange)) {
				HashMap<String, Object> object = new HashMap<>();
				object.put("rate", daq.getFedBuilderSummary().getRate());
				object.put("sumEventsInRU", daq.getFedBuilderSummary().getSumEventsInRU());
				object.put("sumRequests", daq.getFedBuilderSummary().getSumRequests());
				fedBuilderSummaries.add(object);
				dates.add(df.format(new Date(daq.getLastUpdate())));
			}
		}

		HashMap<String, Object> result = new HashMap<>();
		result.put("fbs", fedBuilderSummaries);
		result.put("dates", dates);

		ObjectMapper objectMapper = new ObjectMapper();

		String json = objectMapper.writeValueAsString(result);

		// String json = new Gson().toJson(result);

		// logger.info("Response JSON: " + json);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		response.getWriter().write(json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}