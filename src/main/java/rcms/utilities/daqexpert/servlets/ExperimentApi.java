package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.Entry;

/**
 * Api to request rerun of experimental logic modules on chosen time span
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ExperimentApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ExperimentApi.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String experimentalLm = request.getParameter("experimental-lm");
		// experimentalLm = "test";

		logger.info("Experimental run of LM: " + experimentalLm);

		Set<Entry> destination = new LinkedHashSet<Entry>();
		Application.get().getDataManager().experimental.put(experimentalLm, destination);

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");

		Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
		Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();

		logger.info("Experimenting on from : " + startDate + " to " + endDate);

		Future<?> future = Application.get().getJobManager().fireOnDemandJob(startDate.getTime(), endDate.getTime(),
				destination, experimentalLm);

		HashMap<String, Object> result = new HashMap<>();
		String message = null;
		String status = "fail";

		try {
			future.get(30, TimeUnit.SECONDS);
			status = "success";
		} catch (ExecutionException e) {


			logger.fatal("Execution exception catched: " + e.getMessage());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			message = sw.toString();
			
		} catch (InterruptedException e) {
			message = e.getMessage();
			logger.fatal("Interrupted exception catched");
		} catch (DAQException e) {
			message = e.getMessage();
			logger.fatal("DAQException exception catched");
		} catch (TimeoutException e) {
			message = e.getMessage();
			logger.fatal("TimeoutException exception catched");
		}
		
		result.put("message", message);
		result.put("status", status);

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(result);
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