package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.reasoning.base.Entry;

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
		experimentalLm = "test";
		
		logger.info("Experimental run of LM: " + experimentalLm);

		Set<Entry> destination = new LinkedHashSet<Entry>();
		Application.get().getDataManager().experimental.put(experimentalLm, destination);

		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");

		Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
		Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();

		logger.info("Experimenting on from : " + startDate + " to " + endDate);

		Future future = Application.get().getJobManager().fireOnDemandJob(startDate.getTime(), endDate.getTime(),
				destination);


		String result = null;
		
		try {
			future.get();
			result = "successful";
		} catch (InterruptedException e) {
			e.printStackTrace();
			result = e.getMessage();
		} catch (ExecutionException e) {
			e.printStackTrace();
			result = e.getMessage();
		}
		
		
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