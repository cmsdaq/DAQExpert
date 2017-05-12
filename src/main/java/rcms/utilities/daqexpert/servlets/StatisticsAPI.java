package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.Application;
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
		logger.debug("Getting statistics from : " + startRange + " to " + endRange);

		Date startDate = null;
		Date endDate = null;
		try {
			startDate = DatatypeConverter.parseDateTime(startRange).getTime();
			endDate = DatatypeConverter.parseDateTime(endRange).getTime();
		} catch (NullPointerException e) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, -24);
			startDate = cal.getTime();
			cal.add(Calendar.HOUR, -24 * 365);
			endDate = cal.getTime();
		}

		logger.info("Parsed range from : " + startDate + " to " + endDate);

		Report report = Application.get().getReportManager().prepareReport(startDate, endDate);

		logger.info(report.getSummary());

		request.setAttribute("summary", report.getSummary());

		request.getRequestDispatcher("/statistics.jsp").forward(request, response);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}