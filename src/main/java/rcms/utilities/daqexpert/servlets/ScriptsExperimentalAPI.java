package rcms.utilities.daqexpert.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.reasoning.processing.ExperimentalProcessor;

/**
 * Api to request available experimental logic modules
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ScriptsExperimentalAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ScriptsExperimentalAPI.class);

	private ExperimentalProcessor experimentalProcessor;

	public ScriptsExperimentalAPI() {
		try {
			experimentalProcessor = new ExperimentalProcessor(
					Application.get().getProp().getProperty(Application.EXPERIMENTAL_DIR));
		} catch (IOException | ResourceException | ScriptException e) {
			experimentalProcessor = null;
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<File> scripts = experimentalProcessor.getScriptFiles();

		logger.info("Available LMs: " + scripts.size());

		Map<String, Object> result = new HashMap<>();

		List<String> names = new ArrayList<>();

		for (File file : scripts) {
			names.add(file.getName());
		}

		result.put("names", names);
		result.put("directory", experimentalProcessor.getExperimentalDirectory());

		logger.info("Available LMs: " + names);
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