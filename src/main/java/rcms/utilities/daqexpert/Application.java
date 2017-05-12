package rcms.utilities.daqexpert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.processing.JobManager;
import rcms.utilities.daqexpert.report.Report;
import rcms.utilities.daqexpert.report.ReportManager;
import rcms.utilities.daqexpert.websocket.ConditionDashboard;

public class Application {

	private static final Logger logger = Logger.getLogger(Application.class);

	private PersistenceManager persistenceManager;
	
	private ReportManager reportManager;

	private DataManager dataManager;

	private JobManager jobManager;

	private final Properties prop;

	private final ConditionDashboard conditionDashboard;

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
					throw new ExpertException(ExpertExceptionCode.MissingProperty,
							": Required property missing " + setting.getKey());
			}
		}
	}

	public static void initialize(String propertiesFile) {
		instance = new Application(propertiesFile);
		checkRequiredSettings();
		String v = instance.getClass().getPackage().getImplementationVersion();
		logger.info("DAQExpert version: " + v);

		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("history", instance.getProp());
		instance.reportManager = new ReportManager(entityManagerFactory);
		instance.persistenceManager = new PersistenceManager(entityManagerFactory);
		instance.setDataManager(new DataManager());
	}

	private Application(String propertiesFile) {
		this.prop = load(propertiesFile);
		conditionDashboard = new ConditionDashboard(5);
	}

	private static Application instance;

	private Properties load(String propertiesFile) {

		try {

			logger.info("Loading properties from environment variable location");
			FileInputStream propertiesInputStream = new FileInputStream(propertiesFile);
			Properties properties = new Properties();
			properties.load(propertiesInputStream);
			return properties;

		}

		catch (FileNotFoundException e) {
			throw new ExpertException(ExpertExceptionCode.MissingConfigurationFile,
					propertiesFile + " cannot be found. " + e.getMessage());
		} catch (IOException e) {
			throw new ExpertException(ExpertExceptionCode.MissingConfigurationFile, e.getMessage());
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

	public ConditionDashboard getDashboard() {
		return conditionDashboard;
	}

	public ReportManager getReportManager() {
		return reportManager;
	}
}
