package rcms.utilities.daqexpert.reasoning.processing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;

/**
 * Loads and uses experimental Logic Modules
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ExperimentalProcessor {

	private List<Pair<Class<LogicModule>, Object>> scriptInstances;

	private final GroovyScriptEngine groovyScriptEngine;

	FileSystemConnector connector;
	String experimentalDirectory;

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
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run experimental Logic Modules
	 * 
	 * @param daq
	 * @param checkerResultMap
	 */
	public void runLogicModules(DAQ daq, Map<String, Boolean> checkerResultMap) {

		if (scriptInstances == null) {
			throw new RuntimeException("No experimental logic modules have been loaded");
		}

		for (Pair<Class<LogicModule>, Object> scriptInstance : scriptInstances) {
			runExperimental(daq, checkerResultMap, scriptInstance.getLeft(), scriptInstance.getRight());
		}

	}

	/**
	 * Loads
	 * 
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected List<Pair<Class<LogicModule>, Object>> loadScriptInstances()
			throws InstantiationException, IllegalAccessException {

		List<Pair<Class<LogicModule>, Object>> result = new ArrayList<>();
		List<Class<LogicModule>> scriptClasses = getScripts();

		for (Class<LogicModule> scriptClass : scriptClasses) {
			Object scriptInstance = scriptClass.newInstance();
			result.add(Pair.of(scriptClass, scriptInstance));
		}

		return result;
	}

	protected List<Class<LogicModule>> getScripts() {

		try {
			List<File> a = connector.getFiles(experimentalDirectory);
			logger.info("Discovered " + a.size() + " scripts in " + experimentalDirectory);

			List<Class<LogicModule>> scripts = new ArrayList<>();

			for (File b : a) {

				if (b.getName().endsWith(".java") || b.getName().endsWith(".groovy")) {
					try {
						Class<LogicModule> scriptClass = loadFromFile(b);
						scripts.add(scriptClass);
					} catch (ResourceException e) {
						e.printStackTrace();
					} catch (ScriptException e) {
						e.printStackTrace();
					}
				}
			}

			return scripts;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Class<LogicModule> loadFromFile(File file) throws ResourceException, ScriptException {
		logger.info("Loading script: " + file.getAbsolutePath());
		return groovyScriptEngine.loadScriptByName(file.getName());
	}

	protected void runExperimental(DAQ daq, Map<String, Boolean> checkerResultMap, Class<LogicModule> scriptClass,
			Object scriptInstance) {
		try {
			Object gresult = scriptClass.getDeclaredMethod("satisfied", DAQ.class, Map.class).invoke(scriptInstance,
					daq, checkerResultMap);

			if (gresult instanceof Boolean) {
				checkerResultMap.put(scriptClass.getSimpleName(), (boolean) gresult);
			} else {
				logger.info("Groovy returned non-boolean value: " + gresult + ", cannot add to results");
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}
