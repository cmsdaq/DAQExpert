package rcms.utilities.daqexpert.servlets;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.ExpertPersistorManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.JobManager;
import rcms.utilities.daqexpert.segmentation.DataResolutionManager;

public class ServletListener implements ServletContextListener {

	public ServletListener() {

		super();
		String propertyFilePath = System.getenv("EXPERT_CONF");
		if (propertyFilePath == null) {
			logger.info("No configuration file supplied with environment variable EXPERT_CONF");
		}

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("history");
		logger.info("Persistence initialization finished");

		Application.initialize(propertyFilePath);

		String snapshotsDir = Application.get().getProp(Setting.SNAPSHOTS_DIR);

		if (snapshotsDir != null)
			logger.info("Loading snapshots from directory: " + snapshotsDir);
		else {
			logger.warn("Could not load snapshot directory from neither SNAPSHOTS env var nor config.properties file");
			return;
		}

		persistorManager = new ExpertPersistorManager(snapshotsDir);

	}

	private static final Logger logger = Logger.getLogger(ServletListener.class);

	ExpertPersistorManager persistorManager;
	DataResolutionManager dataSegmentator;

	public void contextInitialized(ServletContextEvent e) {
		String sourceDirectory = Application.get().getProp(Setting.SNAPSHOTS_DIR);

		DataManager dataManager = Application.get().getDataManager();

		JobManager jobManager = new JobManager(sourceDirectory, dataManager);
		jobManager.startJobs();

		Application.get().setJobManager(jobManager);
	}

	public void contextDestroyed(ServletContextEvent e) {
	}

}