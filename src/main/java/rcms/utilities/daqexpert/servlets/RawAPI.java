package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.Point;

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

		Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
		Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();

		logger.debug("Parsed range from : " + startDate + " to " + endDate);

		List<Point> targetData = Application.get().getPersistenceManager().getRawData(startDate, endDate);

		ObjectMapper objectMapper = new ObjectMapper();

		String json = objectMapper.writeValueAsString(targetData);

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		logger.debug("Number of elements returned: " + targetData.size());

		response.getWriter().write(json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}