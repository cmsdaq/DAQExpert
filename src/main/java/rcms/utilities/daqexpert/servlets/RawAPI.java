package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.DataManager;

/**
 * Event occurrences servlet API, used for async requests in autoupdate mode.
 * 
 * This API is used by main servlet for event occurrences view.
 * 
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RawAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ObjectMapper objectMapper = new ObjectMapper();

	private static final Logger logger = Logger.getLogger(RawAPI.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");
		logger.debug("Getting reasons from : " + startRange + " to " + endRange);

		Date startDate = objectMapper.readValue(startRange, Date.class);
		Date endDate = objectMapper.readValue(endRange, Date.class);

		// extend slightly timespan so that few snapshots more on the left and
		// right are loaded to the chart - avoid cutting the chart lines
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.SECOND, -10);
		startDate = c.getTime();
		c.setTime(endDate);
		c.add(Calendar.SECOND, 10);
		endDate = c.getTime();

		logger.debug("Parsed range from : " + startDate + " to " + endDate);

		List<HashMap<String, Long>> data = new ArrayList<>();

		// SortedSet<HashMap<String, Long>> data = new TreeSet<>();

		RangeResolver rangeResolver = new RangeResolver();
		DataResolution range = rangeResolver.resolve(startDate, endDate);

		List<DummyDAQ> targetData = null;
		switch (range) {
		case Full:
			targetData = DataManager.get().rawData;
			break;
		case Minute:
			targetData = DataManager.get().rawDataMinute;
			break;
		case Hour:
			targetData = DataManager.get().rawDataHour;
			break;
		case Day:
			targetData = DataManager.get().rawDataDay;
			break;
		default:
			targetData = DataManager.get().rawDataMonth; // TODO: change to
															// MONTH
			break;
		}

		synchronized (targetData) {
			for (DummyDAQ daq : targetData) {
				if (daq.getLastUpdate() >= startDate.getTime() && daq.getLastUpdate() <= endDate.getTime()) {
					HashMap<String, Long> rateObject = new HashMap<>();
					HashMap<String, Long> eventObject = new HashMap<>();

					// rate in kHz
					rateObject.put("y", daq.getRate());

					// milions of events
					eventObject.put("y", (long) daq.getEvents());
					rateObject.put("x", daq.getLastUpdate());
					eventObject.put("x", daq.getLastUpdate());
					rateObject.put("group", 0L);
					eventObject.put("group", 1L);
					data.add(rateObject);
					data.add(eventObject);
				}
			}
		}

		logger.debug("Range: " + range + ", elements to process: " + targetData.size());

		ObjectMapper objectMapper = new ObjectMapper();

		String json = objectMapper.writeValueAsString(data);

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		logger.debug("Number of elements returned: " + data.size() + ", using " + range + " data");

		response.getWriter().write(json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}