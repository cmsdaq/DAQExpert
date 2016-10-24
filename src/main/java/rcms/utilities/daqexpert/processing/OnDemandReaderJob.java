package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;

/**
 * Job for retrieving data on demand (snapshots)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class OnDemandReaderJob implements ReaderJob {
	/**
	 * Indicator of last snapshot processed timestamp
	 */
	private long readFrom;

	private final PersistenceExplorer explorer;

	private long readTo;

	private final String sourceDirectory;

	private volatile boolean finished = false;

	private static final Logger logger = Logger.getLogger(OnDemandReaderJob.class);

	public OnDemandReaderJob(PersistenceExplorer explorer, String sourceDirectory) {
		super();
		this.explorer = explorer;
		this.sourceDirectory = sourceDirectory;
	}

	public void setTimeSpan(long readFrom, long readTo) {
		this.readFrom = readFrom;
		this.readTo = readTo;
	}

	@Override
	public Pair<Long, List<File>> read() {

		finished = false;

		logger.info("Requested snapshots in timespan " + new Date(readFrom) + "-" + new Date(readTo)
				+ " elements on demand");
		try {

			Pair<Long, List<File>> entry;

			do {
				Pair<Date, Date> currentStep = step();
				logger.debug("Getting chunk " + currentStep.getLeft() + " - " + currentStep.getRight());
				entry = explorer.explore(currentStep.getLeft().getTime(), currentStep.getRight().getTime(),
						sourceDirectory);
				if (entry.getRight().size() > 0)
					logger.info("In this chunk " + entry.getRight().size() + " elements");
				readFrom = currentStep.getRight().getTime();
			} while (entry.getRight().size() == 0 && !finished);

			// remember last explored snapshot timestamp
			readFrom = entry.getKey();

			logger.info("Found " + entry.getRight().size() + " elements on demand");

			return entry;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	private Pair<Date, Date> step() {
		Date chunkStart = new Date(readFrom);
		Date chunkEnd = new Date(getForwardHourChunk(readFrom));

		if (getForwardHourChunk(readFrom) > readTo) {
			chunkEnd = new Date(readTo);
			finished = true;
		}

		return Pair.of(chunkStart, chunkEnd);
	}

	private long getForwardHourChunk(long time) {
		Date date = new Date(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR, 1);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime().getTime();
	}

	@Override
	public boolean finished() {
		return false;
	}

}
