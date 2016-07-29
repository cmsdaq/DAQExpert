package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.ContextCollector;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventFinder;
import rcms.utilities.daqexpert.reasoning.base.EventProducer;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;

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

			Map<String, Object> result = new HashMap<>();
			List<Entry> entries = EventProducer.get().getResult();
			for (Entry entry : entries) {
				// FIXME: map should be used
				if (entry.getId() == id) {

					String description;
					EventFinder eventFinder = entry.getEventFinder();

					/* Case of Extended condition */
					if (eventFinder instanceof ExtendedCondition) {
						ExtendedCondition extendedCondition = (ExtendedCondition) eventFinder;
						ContextCollector context = entry.getFinishedContext();
						if (context != null) {
							description = context.getMessageWithContext(extendedCondition.getDescription());
						} else {
							description = extendedCondition.getDescription();
						}

						result.put("description", description);
						result.put("action", extendedCondition.getAction());
						result.put("elements", entry.getFinishedContext().getContext());
					}

					/* case of every other condition */
					else {
						description = eventFinder.getDescription();
					}

					result.put("description", description);
					result.put("name", entry.getContent());
				}

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