package rcms.utilities.daqexpert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.persistence.SnapshotFormat;
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

	public ExpertPersistorManager(String persistenceDir) {
		super(persistenceDir, SnapshotFormat.SMILE);
		instance = this;
	}

	private static ExpertPersistorManager instance;
	private ObjectMapper objectMapper = new ObjectMapper();

	public static ExpertPersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}
	

	public Entry<Long,List<File>> explore(Long startTimestamp) throws IOException {
		return explore(startTimestamp, Long.MAX_VALUE);
	}

	/**
	 * 
	 * @param startTimestamp
	 * @return returns timestamp of last snapshot explored and list of explored snapshots
	 * @throws IOException
	 */
	public Entry<Long,List<File>> explore(Long startTimestamp, Long endTimestamp) throws IOException {

		Long tmpLast = startTimestamp;
		Long startTime = System.currentTimeMillis();
		Long snapshotCount = 0L;
		
		List<File> result = new ArrayList<>();

		List<File> yearDirs = getDirs(persistenceDir);

		for (File dirYear : yearDirs) {
			List<File> monthDirs = getDirs(dirYear.getAbsolutePath());

			for (File monthDir : monthDirs) {
				List<File> dayDirs = getDirs(monthDir.getAbsolutePath());

				for (File dayDir : dayDirs) {
					List<File> hourDirs = getDirs(dayDir.getAbsolutePath());

					for (File hourDir : hourDirs) {
						List<File> snapshots = getFiles(hourDir.getAbsolutePath());

						for (File snapshot : snapshots) {

							int dotIdx = snapshot.getName().indexOf(".");

							if (dotIdx != -1) {
								Long timestamp = Long.parseLong(snapshot.getName().substring(0, dotIdx));

								// FIXME: this needs to be improved
								if (startTimestamp < timestamp && timestamp < endTimestamp && snapshotCount < 2000) {
									startTimestamp = timestamp;
									result.add(snapshot);
									snapshotCount++;
								}
							}
						}

					}
				}
			}
		}

		Long endTime = System.currentTimeMillis();
		logger.debug("Explored " + snapshotCount + " snapshots (" + result.size() + " snapshots) after " + tmpLast
				+ ", in " + (endTime - startTime) + "ms");
		Entry<Long, List<File>> entry = new SimpleEntry<>(startTimestamp, result);
		return entry;
	}

	protected List<File> getDirs(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);

		if (folder.exists() && folder.isDirectory()) {

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					System.out.println("File " + listOfFiles[i].getName());
				} else if (listOfFiles[i].isDirectory()) {
					// directory name must be always parsable integer
					try{
						Integer.parseInt(listOfFiles[i].getName());
						result.add(listOfFiles[i]);
					} catch(NumberFormatException e){
						// ignore directory
					}
				}
			}
			Collections.sort(result, DirComparator);

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

	protected List<File> getFiles(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);

		if (folder.exists() && folder.isDirectory()) {

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					result.add(listOfFiles[i]);
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}
			Collections.sort(result, FileComparator);

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

	public static Comparator<File> DirComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			Integer filename1 = Integer.parseInt(path1.getName().toString());
			Integer filename2 = Integer.parseInt(path2.getName().toString());
			return filename1.compareTo(filename2);
		}
	};

	public static Comparator<File> FileComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			String filename1 = path1.getName().toString();
			String filename2 = path2.getName().toString();
			return filename1.compareTo(filename2);
		}
	};

	/**
	 * Find snapshot which is the closest to given date
	 * 
	 * @param date
	 *            requested date to find snapshot
	 * @return DAQ snapshot found for given date
	 * @throws IOException
	 */
	public DAQ findSnapshot(Date date) throws IOException {
		List<File> candidates = new ArrayList<>();
		String candidateDir = null;

		logger.info("Searching snapshot for date: " + date + ", base dir: " + persistenceDir);

		candidateDir = this.getTimeDir(persistenceDir, date);
		
		logger.info("Candidates will be searched in " + candidateDir);

		try {
			candidates.addAll(getFiles(candidateDir));
		} catch (FileNotFoundException e) {
			candidates = new ArrayList<>();
			logger.warn("Cannot access persisence dir, ignoring...");
		}

		return findSnapshot(date, candidates);

	}

	private DAQ findSnapshot(Date date, List<File> candidates) {
		StructureSerializer structurePersistor = new StructureSerializer();
		try {

			if (candidates.size() == 0) {
				logger.error("No files to process");
				return null;
			}
			Collections.sort(candidates, FileComparator);

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

			logger.info("Best file found: " + bestFile + " with time diff: " + diff + "ms.");
			best = structurePersistor.deserialize(bestFile,SnapshotFormat.SMILE);
			return best;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
