package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistorManager;

public class SnapshotAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SnapshotAPI.class);


	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String time = request.getParameter("time");
		logger.info("Requested snapshot date: " + time);
		Date timeDate = objectMapper.readValue(time, Date.class);
		logger.info("Parsed requested snapshot date: " + timeDate);

		DAQ result = PersistorManager.get().findSnapshot(timeDate);

		String json = objectMapper.writeValueAsString(result);
		// TODO: externalize the Allow-Origin
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

		logger.info("Found snapshot with timestamp: " + new Date(result.getLastUpdate()) + ": " + json.substring(0,1000));

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}