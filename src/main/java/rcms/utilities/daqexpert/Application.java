package rcms.utilities.daqexpert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.processing.JobManager;

public class Application {

	private static final Logger logger = Logger.getLogger(Application.class);

	public static final String NM_DASHBOARD = "nm.dashboard";
	public static final String NM_NOTIFICATIONS = "nm.notifications";
	public static final String NM_API_CREATE = "nm.api.create";
	public static final String NM_API_CLOSE = "nm.api.close";
	public static final String SNAPSHOTS_DIR = "snapshots";
	public static final String LANDING = "landing";
	public static final String OFFSET = "nm.offset";
	public static final String EXPERIMENTAL_DIR = "experimental";

	private DataManager dataManager;

	private JobManager jobManager;

	private final Properties prop;

	public static Application get() {
		if (instance == null) {
			throw new RuntimeException("Not initialized");
		}
		return instance;
	}

	public static void initialize(String propertiesFile) {
		String message = "Required property missing ";
		instance = new Application(propertiesFile);
		if (!instance.prop.containsKey(NM_DASHBOARD))
			throw new RuntimeException(message + NM_DASHBOARD);
		if (!instance.prop.containsKey(NM_DASHBOARD))
			throw new RuntimeException(message + NM_DASHBOARD);
		if (!instance.prop.containsKey(SNAPSHOTS_DIR))
			throw new RuntimeException(message + SNAPSHOTS_DIR);
		if (!instance.prop.containsKey(NM_API_CREATE))
			throw new RuntimeException(message + NM_API_CREATE);
		if (!instance.prop.containsKey(NM_API_CLOSE))
			throw new RuntimeException(message + NM_API_CLOSE);
		if (!instance.prop.containsKey(LANDING))
			throw new RuntimeException(message + LANDING);
		if (!instance.prop.containsKey(OFFSET))
			throw new RuntimeException(message + OFFSET);
		if (!instance.prop.containsKey(EXPERIMENTAL_DIR))
			throw new RuntimeException(message + EXPERIMENTAL_DIR);
	}

	private Application(String propertiesFile) {
		this.prop = load(propertiesFile);
		this.setDataManager(new DataManager());
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
}
