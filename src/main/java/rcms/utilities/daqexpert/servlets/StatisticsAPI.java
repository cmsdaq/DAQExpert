package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.Calendar;
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
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.report.Report;

/**
 * Statistics API.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class StatisticsAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ObjectMapper objectMapper = new ObjectMapper();

	private static final Logger logger = Logger.getLogger(StatisticsAPI.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");
		logger.info("Getting statistics from : " + startRange + " to " + endRange);

		Date startDate = null;
		Date endDate = null;
		try {
			startDate = DatatypeConverter.parseDateTime(startRange).getTime();
			endDate = DatatypeConverter.parseDateTime(endRange).getTime();
		} catch (NullPointerException e) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, -24);
			endDate = cal.getTime();
			cal.add(Calendar.HOUR, -24 * 7);
			startDate = cal.getTime();
		}

		logger.info("Parsed range from : " + startDate + " to " + endDate);

		Report report = Application.get().getReportManager().prepareReport(startDate, endDate);
		List<Long> noRateWhenExpectedHistogram = Application.get().getReportManager()
				.getHistogram(LogicModuleRegistry.NoRateWhenExpected, startDate, endDate, 1000L, 1000L * 60 * 60 * 24);
		List<Long> stableBeamsHistogram = Application.get().getReportManager()
				.getHistogram(LogicModuleRegistry.StableBeams, startDate, endDate, 1000L, 1000L * 60 * 60 * 24 * 30);
		List<Long> runOngoingHistogram = Application.get().getReportManager()
				.getHistogram(LogicModuleRegistry.RunOngoing, startDate, endDate, 1000L, 1000L * 60 * 60 * 24);

		logger.info(report.getSummary());
		logger.info(noRateWhenExpectedHistogram);
		logger.info(stableBeamsHistogram);
		logger.info(runOngoingHistogram);

		request.setAttribute("startdate", startDate);
		request.setAttribute("enddate", endDate);
		request.setAttribute("summary", report.getSummary());
		request.setAttribute("runongoinghistogram", objectMapper.writeValueAsString(runOngoingHistogram));
		request.setAttribute("nrwehistogram", objectMapper.writeValueAsString(noRateWhenExpectedHistogram));
		request.setAttribute("stablebeamshistogram", objectMapper.writeValueAsString(stableBeamsHistogram));

		request.getRequestDispatcher("/statistics.jsp").forward(request, response);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}