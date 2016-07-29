package rcms.utilities.daqexpert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.persistence.SnapshotFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.reasoning.base.CheckManager;
import rcms.utilities.daqexpert.reasoning.base.EventProducer;
import rcms.utilities.daqexpert.reasoning.base.SnapshotProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

/**
 * This class reads the persisted DAQ snapshots and
 * 
 * TODO: passes it to registered listeners
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class ExpertPersistorManager extends PersistorManager {

	private static final Logger logger = Logger.getLogger(ExpertPersistorManager.class);

	public ExpertPersistorManager(String persistenceDir) {
		super(persistenceDir, SnapshotFormat.SMILE);
		instance = this;
	}

	private static ExpertPersistorManager instance;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private SnapshotProcessor snapshotProcessor = new SnapshotProcessor();
	private static String updatedDir;

	public static ExpertPersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}

	public boolean getUnprocessedSnapshots(Map<String, File> processed) throws IOException {

		try {
			List<File> fileList = getFiles(updatedDir);
			Collections.sort(fileList, FileComparator);

			StructureSerializer structurePersistor = new StructureSerializer();
			DAQ daq = null;
			logger.debug("Processing files from " + updatedDir + "...");

			int max = 5000;
			int i = 0;
			boolean breaked = false;
			for (File path : fileList) {
				if (!processed.containsKey(path.getName())) {
					try {
						i++;
						daq = structurePersistor.deserializeFromSmile(path.getAbsolutePath().toString());
						
						if(daq == null){
							logger.error("Snapshot not deserialized " + path.getAbsolutePath());
						}
						
						TaskManager.get().rawData.add(new DummyDAQ(daq));
						
//						try {
//							Thread.sleep(600);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
						snapshotProcessor.process(daq);

						processed.put(path.getName(), path);
						if (i > max) {
							breaked = true;
							break;
						}
					} catch (RuntimeException e) {
						logger.error("Problem processing snapshot " + path.getAbsolutePath().toString() , e);
					}
				}
			}

			// temporarly finish
			if (daq != null)
				EventProducer.get().finish(new Date(daq.getLastUpdate()));

			return breaked;
		} catch (NullPointerException e) {
			logger.error("Problem getting snapthot files from: " + updatedDir);
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * This method walks through all files but does not keep them in memory. It
	 * runs analysis modules and saves the results.
	 * 
	 * @throws IOException
	 */
	@Deprecated
	private void walkAll() throws IOException {

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
			checkManager.runLogicModules(daq);
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

	public DAQ findSnapshot(Date date) {
		StructureSerializer structurePersistor = new StructureSerializer();
		try {
			List<File> fileList = new ArrayList<>();
			try {
				fileList.addAll(getFiles(persistenceDir));
			} catch (FileNotFoundException e) {
				fileList = new ArrayList<>();
				logger.warn("Cannot access persisence dir, ignoring...");
			}

			try {
				fileList.addAll(getFiles(updatedDir));
			} catch (FileNotFoundException e) {
				fileList = new ArrayList<>();
				logger.warn("Cannot access snapshots dir, ignoring...");
			}

			if (fileList.size() == 0) {
				logger.error("No files to process");
				return null;
			}
			Collections.sort(fileList, FileComparator);

			long diff = Integer.MAX_VALUE;
			String bestFile = null;
			DAQ best = null;
			for (File path : fileList) {

				String currentName = path.getAbsolutePath().toString();
				String dateFromFileName = path.getName();
				if (dateFromFileName.contains(".")) {
					int indexOfDot = dateFromFileName.indexOf(".");
					dateFromFileName = dateFromFileName.substring(0, indexOfDot);
				}
				Date currentDate;
				currentDate = objectMapper.readValue(dateFromFileName, Date.class);

				logger.trace("Current file: " + currentName);

				if (bestFile == null) {
					bestFile = currentName;
					continue;
				}

				long currDiff = date.getTime() - currentDate.getTime();

				if (Math.abs(currDiff) < diff) {
					bestFile = currentName;
					diff = Math.abs(currDiff);
				}
			}

			logger.info("Best file found: " + bestFile + " with time diff: " + diff + "ms.");
			best = structurePersistor.deserializeFromSmile(bestFile);
			return best;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	public static void setUpdatedDir(String updatedDir) {
		ExpertPersistorManager.updatedDir = updatedDir;
	}

}
