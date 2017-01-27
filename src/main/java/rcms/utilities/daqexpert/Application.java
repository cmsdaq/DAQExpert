package rcms.utilities.daqexpert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.processing.JobManager;

public class Application {

	private static final Logger logger = Logger.getLogger(Application.class);

	private PersistenceManager persistenceManager;

	private DataManager dataManager;

	private JobManager jobManager;

	private final Properties prop;

	public static Application get() {
		if (instance == null) {
			throw new RuntimeException("Not initialized");
		}
		return instance;
	}

	/**
	 * Check if all required settings are present in configuration file
	 */
	private static void checkRequiredSettings() {
		for (Setting setting : Setting.values()) {
			if (setting.isRequired()) {
				if (!instance.prop.containsKey(setting.getKey()))
					throw new DAQException(DAQExceptionCode.MissingProperty, ": Required property missing " + setting.getKey());
			}
		}
	}

	public static void initialize(String propertiesFile) {
		instance = new Application(propertiesFile);
		checkRequiredSettings();
		instance.persistenceManager = new PersistenceManager("history");
		instance.setDataManager(new DataManager(instance.persistenceManager));
	}

	private Application(String propertiesFile) {
		this.prop = load(propertiesFile);
	}

	private static Application instance;

	private Properties load(String propertiesFile) {

		try {

			if (propertiesFile == null) {
				logger.info("Loading properties from default location");
				String resourceName = "config.properties"; // could also be a
															// constant
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Properties props = new Properties();
				try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
					props.load(resourceStream);
				}
				return props;

			} else {
				logger.info("Loading properties from environment variable location");
				FileInputStream propertiesInputStream = new FileInputStream(propertiesFile);
				Properties properties = new Properties();
				properties.load(propertiesInputStream);
				return properties;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot run application without configuration file");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot run application without configuration file");
		}
	}

	public String getProp(Setting setting) {
		Object property = prop.get(setting.getKey());
		if (property != null) {
			return property.toString();
		} else {
			throw new RuntimeException("Problem retrieving property: " + setting);
		}
	}

	public Properties getProp() {
		return prop;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	public JobManager getJobManager() {
		return jobManager;
	}

	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}
}
