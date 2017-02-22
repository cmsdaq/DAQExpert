package rcms.utilities.daqexpert.reasoning.processing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import groovy.lang.MissingPropertyException;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

/**
 * Loads and uses experimental Logic Modules
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ExperimentalProcessor {

	private List<Pair<Class<LogicModule>, LogicModule>> scriptInstances;

	private final GroovyScriptEngine groovyScriptEngine;

	private final FileSystemConnector connector;
	private final String experimentalDirectory;

	private String requestedScript;

	private static final Logger logger = Logger.getLogger(ExperimentalProcessor.class);

	public ExperimentalProcessor(String experimentalDirectory) throws IOException, ResourceException, ScriptException {
		this.experimentalDirectory = experimentalDirectory;
		groovyScriptEngine = new GroovyScriptEngine(experimentalDirectory);

		connector = new FileSystemConnector();
	}

	/**
	 * Loads experimental Logic Modules from experimental directory
	 */
	public void loadExperimentalLogicModules() {
		logger.info("Loading experimental Logic Modules from directory " + experimentalDirectory);

		try {
			scriptInstances = loadScriptInstances();
			logger.info("Successfully loaded " + scriptInstances.size() + " script instances");

		} catch (InstantiationException e) {
			// TODO: change this code
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		}
	}

	/**
	 * Run experimental Logic Modules
	 * 
	 * @param daq
	 * @param checkerResultMap
	 * @return returns results of all logic modules as list of pairs
	 *         [LM;satisfied]
	 */
	public List<Pair<LogicModule, Boolean>> runLogicModules(DAQ daq, Map<String, Boolean> checkerResultMap) {

		List<Pair<LogicModule, Boolean>> result = new ArrayList<>();
		if (scriptInstances == null) {
			throw new RuntimeException("No experimental logic modules have been loaded");
		}

		for (Pair<Class<LogicModule>, LogicModule> scriptInstance : scriptInstances) {
			Pair<LogicModule, Boolean> a = runExperimental(daq, checkerResultMap, scriptInstance.getLeft(),
					scriptInstance.getRight());
			result.add(a);
		}
		return result;

	}

	/**
	 * Loads
	 * 
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected List<Pair<Class<LogicModule>, LogicModule>> loadScriptInstances()
			throws InstantiationException, IllegalAccessException {

		List<Pair<Class<LogicModule>, LogicModule>> result = new ArrayList<>();
		List<Class<LogicModule>> scriptClasses = getScripts();

		for (Class<LogicModule> scriptClass : scriptClasses) {
			try {
				LogicModule scriptInstance = scriptClass.newInstance();
				result.add(Pair.of(scriptClass, scriptInstance));
			} catch (MissingPropertyException e) {
				throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
			}
		}

		return result;
	}

	/**
	 * Gets a list of scripts available in experimental dir
	 * 
	 * @return
	 */
	protected List<Class<LogicModule>> getScripts() {

		try {
			List<File> a = getScriptFiles();
			logger.info("Discovered " + a.size() + " scripts in " + experimentalDirectory);

			List<Class<LogicModule>> scripts = new ArrayList<>();

			for (File b : a) {
				if (requestedScript == null || b.getName().equals(requestedScript)) {
					try {
						Class<LogicModule> scriptClass = loadFromFile(b);
						scripts.add(scriptClass);
					} catch (ResourceException e) {
						throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
					} catch (ScriptException e) {
						throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
					}
				}
			}
			return scripts;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<File> getScriptFiles() throws IOException {
		List<File> result = new ArrayList<>();
		for (File file : connector.getFiles(experimentalDirectory)) {
			if (file.getName().endsWith(".java") || file.getName().endsWith(".groovy")) {
				result.add(file);
			}
		}
		logger.info("Scripts filtered by extension and requested name: " + result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private Class<LogicModule> loadFromFile(File file) throws ResourceException, ScriptException {
		logger.info("Loading script: " + file.getAbsolutePath());
		return groovyScriptEngine.loadScriptByName(file.getName());
	}

	protected Pair<LogicModule, Boolean> runExperimental(DAQ daq, Map<String, Boolean> checkerResultMap,
			Class<LogicModule> scriptClass, LogicModule scriptInstance) {
		try {
			Object gresult = scriptClass.getDeclaredMethod("satisfied", DAQ.class, Map.class).invoke(scriptInstance,
					daq, checkerResultMap);

			if (gresult instanceof Boolean) {
				return Pair.of(scriptInstance, (boolean) gresult);
			} else {
				logger.info("Groovy returned non-boolean value: " + gresult + ", cannot add to results");
			}
		} catch (IllegalAccessException e) {
			logger.error("caught exception while running experimental script:", e);
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("caught exception while running experimental script:", e);
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error("caught exception while running experimental script:", e);
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		} catch (NoSuchMethodException e) {
			logger.error("caught exception while running experimental script:", e);
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		} catch (SecurityException e) {
			logger.error("caught exception while running experimental script:", e);
			throw new ExpertException(ExpertExceptionCode.ExperimentalReasoningProblem, e.getMessage());
		}
		return null;
	}

	public String getExperimentalDirectory() {
		return experimentalDirectory;
	}

	public void setRequestedScript(String scriptName) {
		requestedScript = scriptName;
	}

}
