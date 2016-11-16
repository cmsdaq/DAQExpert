package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;

/**
 * This job manages reading and processing the snapshots
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class DataPrepareJob implements Runnable {

	private final ReaderJob readerJob;
	private final ExecutorService executorService;
	private final Logger logger = Logger.getLogger(DataPrepareJob.class);
	private Set<Entry> destination;
	private DataManager dataManager;

	private final SnapshotProcessor snapshotProcessor;

	public DataPrepareJob(ReaderJob readerJob, ExecutorService executorService, Set<Entry> destination,
			DataManager dataManager, SnapshotProcessor snapshotProcessor) {
		super();
		this.readerJob = readerJob;
		this.executorService = executorService;
		this.destination = destination;
		this.dataManager = dataManager;
		this.snapshotProcessor = snapshotProcessor;
	}

	private static int priority = 0;

	@Override
	public void run() {

		try {
			Pair<Long, List<File>> snapshots;

			if (!readerJob.finished()) {
				snapshots = readerJob.read();

				if (priority == Integer.MAX_VALUE)
					priority = 0;
				else
					priority++;

				ProcessJob processJob = new ProcessJob(priority, snapshots.getRight(), dataManager, snapshotProcessor);
				Future<Set<Entry>> future = executorService.submit(processJob);

				Set<Entry> result = future.get(10, TimeUnit.SECONDS);
				if (destination != null) {
					destination.addAll(result);
				} else {
					logger.warn("No desitnation for processing job - results will be forgotten");
				}

			}

		} catch (Exception e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, e.getMessage());
		}

	}

	protected Set<Entry> getDestination() {
		return destination;
	}

	protected void setDestination(Set<Entry> destination) {
		this.destination = destination;
	}

	protected SnapshotProcessor getSnapshotProcessor() {
		return snapshotProcessor;
	}

}
