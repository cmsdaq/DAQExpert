package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqaggregator.reasoning.base.CheckManager;
import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

public class ExpertPersistorManager extends PersistorManager {

	private static final Logger logger = Logger.getLogger(ExpertPersistorManager.class);

	public ExpertPersistorManager(String persistenceDir) {
		super(persistenceDir);
		instance = this;
	}

	private static ExpertPersistorManager instance;

	public static ExpertPersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}

	public void getUnprocessedSnapshots(Map<String, File> processed, CheckManager checkManager) throws IOException {

		List<File> fileList = getFiles(updatedDir);
		Collections.sort(fileList, FileComparator);

		StructureSerializer structurePersistor = new StructureSerializer();
		DAQ daq = null;
		logger.debug("Processing files from " + updatedDir + "...");

		for (File path : fileList) {
			if (!processed.containsKey(path.getName())) {

				daq = structurePersistor.deserializeFromSmile(path.getAbsolutePath().toString());
				checkManager.runCheckers(daq);
				TaskManager.get().rawData.add(new DummyDAQ(daq));
				processed.put(path.getName(), path);
			}
		}

		// temporarly finish
		if (daq != null)
			EventProducer.get().finish(new Date(daq.getLastUpdate()));

	}

	/**
	 * This method walks through all files but does not keep them in memory. It
	 * runs analysis modules and saves the results.
	 * 
	 * @throws IOException
	 */
	public void walkAll() throws IOException {

		Date earliestSnapshotDate = null, latestSnapshotDate;
		List<File> fileList = getFiles(persistenceDir);
		if (fileList.size() == 0) {
			logger.error("No files to process");
			return;
		}
		Collections.sort(fileList, FileComparator);

		StructureSerializer structurePersistor = new StructureSerializer();
		CheckManager checkManager = new CheckManager();
		DAQ daq = null;
		logger.info("Processing files from " + persistenceDir + "...");

		long start = System.currentTimeMillis();
		for (File path : fileList) {
			logger.debug(path.getName().toString());

			daq = structurePersistor.deserializeFromSmile(path.getAbsolutePath().toString());

			if (earliestSnapshotDate == null)
				earliestSnapshotDate = new Date(daq.getLastUpdate());
			// test logic modules
			checkManager.runCheckers(daq);
			TaskManager.get().rawData.add(new DummyDAQ(daq));

		}
		EventProducer.get().finish(new Date(daq.getLastUpdate()));
		latestSnapshotDate = new Date(daq.getLastUpdate());
		long diff = latestSnapshotDate.getTime() - earliestSnapshotDate.getTime();

		long end = System.currentTimeMillis();
		int result = (int) (end - start);
		long hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
		if (hours != 0)
			logger.info("Deserializing and running analysis modules on " + hours + " hours data (" + fileList.size()
					+ " snapshots) finished in " + result + "ms. (1h of data processed in " + result / hours + "ms)");
		logger.debug("Current producer state: " + EventProducer.get().toString());
	}

}
