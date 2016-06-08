package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

public class RaportAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RaportAPI.class);

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idString = request.getParameter("id");
		logger.info("Requested explanation of event: " + idString);
		try {
			int id = Integer.parseInt(idString);

			Object result = null;
			List<Entry> entries = EventProducer.get().getResult();
			for (Entry entry : entries) {
				if (entry.getId() == id)
					result = entry.getEventRaport();
			}

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

		} catch (NumberFormatException e) {
			logger.warn("There was problem parsing number in raport api request: " + e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}