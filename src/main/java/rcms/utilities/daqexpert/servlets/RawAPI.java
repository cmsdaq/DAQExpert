package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.segmentation.Point;
import rcms.utilities.daqexpert.segmentation.RangeResolver;

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

		Map<DataStream, List<Point>> targetData = null;
		DataManager dataManager = Application.get().getDataManager();
		targetData = dataManager.getRawDataByResolution().get(range);

		logger.debug("resolution of data to RAW API " + range);

		synchronized (targetData) {
			for (Point daq : targetData.get(DataStream.RATE)) {
				if (daq.x >= startDate.getTime() && daq.x <= endDate.getTime()) {
					HashMap<String, Long> rateObject = new HashMap<>();

					// rate in kHz
					rateObject.put("y", (long) daq.y);

					rateObject.put("x", daq.x);
					rateObject.put("group", 0L);
					data.add(rateObject);
				}
			}

			for (Point daq : targetData.get(DataStream.EVENTS)) {
				if (daq.x >= startDate.getTime() && daq.x <= endDate.getTime()) {
					HashMap<String, Long> eventObject = new HashMap<>();

					// rate in kHz
					eventObject.put("y", (long) daq.y);

					eventObject.put("x", daq.x);
					eventObject.put("group", 1L);
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