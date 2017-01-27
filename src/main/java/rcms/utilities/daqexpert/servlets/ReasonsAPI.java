package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.Entry;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;

public class ReasonsAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ReasonsAPI.class);

	int maxDuration = 1000000;
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String experimentalKey = request.getParameter("mode");

		Date fakeEnd = Application.get().getDataManager().getLastUpdate();

		/* requested range dates */
		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");
		logger.debug("Getting reasons from : " + startRange + " to " + endRange);

		/* parsed range dates */

		Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
		Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();
		logger.trace("Parsed range from : " + startDate + " to " + endDate);
		Map<String, Object> result = new HashMap<>();

		List<Entry> entryList = new ArrayList<>();

		Map<String, Long> durations = new HashMap<>();

		Collection<Entry> allElements = null;
		if (experimentalKey == null || experimentalKey.equals("standard")) {
			logger.debug("API runs in standard mode");
			allElements = Application.get().getPersistenceManager().getEntriesWithMask(startDate, endDate);
		} else {
			logger.debug("API runs in experimental mode: " + experimentalKey);
			allElements = Application.get().getDataManager().experimental.get(experimentalKey);
		}

		if (allElements != null) {

			logger.info("There are " + allElements + " in Database");

			for (Entry entry : allElements) {

				try {

					/** durations */
					if ((entry.getGroup() == EventGroup.LHC_BEAM.getCode() && entry.getContent().equals("STABLE BEAMS"))
							|| entry.getGroup() == EventGroup.DOWNTIME.getCode()
							|| entry.getGroup() == EventGroup.AVOIDABLE_DOWNTIME.getCode()) {
						long current = 0;
						if (durations.containsKey(entry.getGroup())) {
							current = durations.get(entry.getGroup());
						}
						durations.put(entry.getGroup(), current + entry.getDuration());
					}

					entryList.add(entry);

				} catch (NullPointerException e) {
					// it means that some of reasons are being builed by
					// event
					// builder, just dont show them yet, they will be
					// ready
					// on
					// next
					// request
				}

			}
		} else {
			logger.warn("There is no data for reasons API. It will return nothing.");
		}

		/*
		 * Remove these headers in production (only for accessing from external
		 * scripts)
		 */
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		/* necessary headers */
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		result.put("entries", entryList);
		result.put("durations", durations);
		result.put("fake-end", fakeEnd);
		/* return the response */
		String json = objectMapper.writeValueAsString(result);
		logger.debug("Response JSON: " + json);
		response.getWriter().write(json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}