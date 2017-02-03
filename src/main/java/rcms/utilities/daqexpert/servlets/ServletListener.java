package rcms.utilities.daqexpert.servlets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.ExpertPersistorManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.JobManager;
import rcms.utilities.daqexpert.segmentation.DataResolutionManager;

public class ServletListener implements ServletContextListener {

	public ServletListener() {

		super();
		String propertyFilePath = null;
		try {
			propertyFilePath = System.getenv("EXPERT_CONF");
			if (propertyFilePath == null) {
				throw new ExpertException(ExpertExceptionCode.MissingConfigurationFile,
						"No configuration file supplied with environment variable EXPERT_CONF");
			}

			logger.info("Persistence initialization finished");

			Application.initialize(propertyFilePath);

			String snapshotsDir = Application.get().getProp(Setting.SNAPSHOTS_DIR);

			if (snapshotsDir != null)
				logger.info("Loading snapshots from directory: " + snapshotsDir);
			else {
				logger.warn(
						"Could not load snapshot directory from neither SNAPSHOTS env var nor config.properties file");
				return;
			}

			persistorManager = new ExpertPersistorManager(snapshotsDir);
		} catch (ExpertException e) {
			logger.fatal("Failed to start expert: " + e.getCode().getName());
			logger.error(e);
			throw e;
		}

	}

	private static final Logger logger = Logger.getLogger(ServletListener.class);

	ExpertPersistorManager persistorManager;
	DataResolutionManager dataSegmentator;
	JobManager jobManager;

	public void contextInitialized(ServletContextEvent e) {
		String sourceDirectory = Application.get().getProp(Setting.SNAPSHOTS_DIR);

		DataManager dataManager = Application.get().getDataManager();

		jobManager = new JobManager(sourceDirectory, dataManager);
		jobManager.startJobs();

		Application.get().setJobManager(jobManager);
	}

	public void contextDestroyed(ServletContextEvent e) {
		logger.info("Expert will go down now, starting shutdown sequence");

		jobManager.stop();

		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				logger.info(String.format("deregistering jdbc driver: %s", driver));
			} catch (SQLException ex) {
				logger.error(String.format("Error deregistering driver %s", driver), ex);
			}

		}
		logger.info("Shutdown sequence completed, expert is down");
	}

}