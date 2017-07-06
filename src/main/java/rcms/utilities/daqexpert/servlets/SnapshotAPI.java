package rcms.utilities.daqexpert.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.util.RawValue;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.ExpertPersistorManager;

/**
 * Request snapshots API
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SnapshotAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SnapshotAPI.class);

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String time = request.getParameter("time");
		logger.debug("Requested snapshot date: " + time);
		Date timeDate = objectMapper.readValue(time, Date.class);
		logger.debug("Parsed requested snapshot date: " + timeDate);
		String json = "";
		try {
			Pair<DAQ,String> fullResult = ExpertPersistorManager.get().findSnapshot(timeDate);

			DAQ result = fullResult.getLeft();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			StructureSerializer ss = new StructureSerializer();
			ss.serialize(result, baos, PersistenceFormat.JSONREFPREFIXED);

			String snapshot = baos.toString(java.nio.charset.StandardCharsets.UTF_8.toString());
			
			RawValue rv = new RawValue(snapshot);
			JsonNode node = JsonNodeFactory.instance.rawValueNode(rv);
			
			Map<String, Object> jsonResult = new HashMap<>();
			jsonResult.put("snapshot",node);
			jsonResult.put("file",fullResult.getRight());
			json = objectMapper.writeValueAsString(jsonResult);
			
			logger.debug("Found snapshot with timestamp: " + new Date(result.getLastUpdate()));
			logger.debug("Snapshot fragment: " + json.substring(0, 1000));
		} catch (RuntimeException e) {
			logger.warn("Requested snapshot with date: " + time + " could not be found");
			Map<String, String> result = new HashMap<>();
			result.put("message", "Could not find snapshot");
			json = objectMapper.writeValueAsString(result);
		}

		// TODO: externalize the Allow-Origin
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

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