package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;

/**
 * Job for reading new snapshots in real time mode
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ForwardReaderJob implements ReaderJob {

	private final static Logger logger = Logger.getLogger(ForwardReaderJob.class);

	private final PersistenceExplorer persistenceExplorer;
	private Long last;
	private final String sourceDirectory;

	public ForwardReaderJob(PersistenceExplorer persistenceExplorer, Long last, String sourceDirectory) {
		super();
		this.persistenceExplorer = persistenceExplorer;
		this.last = last;
		this.sourceDirectory = sourceDirectory;
	}

	@Override
	public Pair<Long, List<File>> read() {
		// get chunk of data
		Pair<Long, List<File>> entry;
		try {
			logger.debug("Exploring with " + last);
			entry = persistenceExplorer.explore(last, sourceDirectory);
			// remember last explored snapshot timestamp
			if (entry != null && entry.getLeft() != null && entry.getLeft() != 0) {

				last = entry.getLeft();
				return entry;
			} else {
				List<File> emptyList = new ArrayList<>();
				return Pair.of(last, emptyList);
			}
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			logger.fatal(e.getMessage());
			throw new DAQException(DAQExceptionCode.ProblemExploringFiles, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new DAQException(DAQExceptionCode.ProblemExploringFiles, "Problem exploring files");
		}

	}

	@Override
	public boolean finished() {
		return false;
	}

}
