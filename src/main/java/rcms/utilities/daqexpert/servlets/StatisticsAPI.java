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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.report.KeyValueReport;

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

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = DatatypeConverter.parseDateTime(startRange).getTime();
			endDate = DatatypeConverter.parseDateTime(endRange).getTime();

			logger.info("Parsed range from : " + startDate + " to " + endDate);

			KeyValueReport report = Application.get().getReportManager().getKeyValueStatistics(startDate, endDate);
			List<Long> noRateWhenExpectedHistogram = Application.get().getReportManager().getHistogram(
					LogicModuleRegistry.NoRateWhenExpected, startDate, endDate, 1000L, 1000L * 60 * 60 * 24);
			List<Long> stableBeamsHistogram = Application.get().getReportManager().getHistogram(
					LogicModuleRegistry.StableBeams, startDate, endDate, 1000L, 1000L * 60 * 60 * 24 * 30);
			List<Long> runOngoingHistogram = Application.get().getReportManager()
					.getHistogram(LogicModuleRegistry.RunOngoing, startDate, endDate, 1000L, 1000L * 60 * 60 * 24);

			logger.info(report.getSummary());
			logger.info(noRateWhenExpectedHistogram);
			logger.info(stableBeamsHistogram);
			logger.info(runOngoingHistogram);

			ArrayNode problemsCausingDeadtime = Application.get().getReportManager().getProblemPieChart(startDate,
					endDate, 1000L);
			ArrayNode subsystemsCausingDeadtime = Application.get().getReportManager().getProblemSubSystems(startDate,
					endDate, 1000L);
			Pair<ArrayNode, ArrayNode> efficiencyResult = Application.get().getReportManager()
					.getDowntimeStatistics(startDate, endDate);

			Map<String, Object> result = new HashMap<>();
			result.put("startdate", startDate);
			result.put("enddate", endDate);
			result.put("runongoinghistogram", runOngoingHistogram);
			result.put("nrwehistogram", noRateWhenExpectedHistogram);
			result.put("stablebeamshistogram", stableBeamsHistogram);
			result.put("piechart1", efficiencyResult.getLeft());
			result.put("piechart2", efficiencyResult.getRight());
			result.put("piechart3", problemsCausingDeadtime);
			result.put("piechart4", subsystemsCausingDeadtime);

			String json = objectMapper.writeValueAsString(result);
			response.getWriter().write(json);

		} catch (NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}