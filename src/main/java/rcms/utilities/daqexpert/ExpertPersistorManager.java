package rcms.utilities.daqexpert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

/**
 * This class reads the persisted DAQ snapshots and
 * 
 * TODO: passes it to registered listeners
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class ExpertPersistorManager extends PersistorManager {

	private static final Logger logger = Logger.getLogger(ExpertPersistorManager.class);

	private final PersistenceExplorer persistenceExplorer;

	public ExpertPersistorManager(String persistenceDir) {
		super(persistenceDir, null, PersistenceFormat.SMILE, null);
		persistenceExplorer = new PersistenceExplorer(new FileSystemConnector());
		instance = this;
	}

	private static ExpertPersistorManager instance;
	private ObjectMapper objectMapper = new ObjectMapper();

	public static ExpertPersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}

	/**
	 * Find snapshot which is the closest to given date
	 * 
	 * @param date
	 *            requested date to find snapshot
	 * @return DAQ snapshot found for given date
	 * @throws IOException
	 */
	public Pair<DAQ,String> findSnapshot(Date date) throws IOException {
		List<File> candidates = new ArrayList<>();
		String candidateDir = null;

		logger.debug("Searching snapshot for date: " + date + ", base dir: " + snapshotPersistenceDir);

		candidateDir = this.getTimeDir(snapshotPersistenceDir, date);

		logger.debug("Candidates will be searched in " + candidateDir);

		try {
			candidates.addAll(persistenceExplorer.getFileSystemConnector().getFiles(candidateDir));
		} catch (FileNotFoundException e) {
			candidates = new ArrayList<>();
			logger.warn("Cannot access persisence dir, ignoring...");
		}

		return findSnapshot(date, candidates);

	}

	private Pair<DAQ,String> findSnapshot(Date date, List<File> candidates) {
		StructureSerializer structurePersistor = new StructureSerializer();
		try {

			if (candidates.size() == 0) {
				logger.error("No files to process");
				return null;
			}
			Collections.sort(candidates, FileSystemConnector.FileComparator);

			long diff = Integer.MAX_VALUE;
			String bestFile = null;
			DAQ best = null;
			for (File path : candidates) {

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

			logger.debug("Best file found: " + bestFile + " with time diff: " + diff + "ms.");
			best = structurePersistor.deserialize(bestFile);
			return Pair.of(best,bestFile);

		} catch (IOException e) {
			logger.error("IO problem finding snapshot", e);
		}

		logger.warn("No snapshot found for date " + date);
		return null;
	}

}
