package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.reasoning.base.Entry;

/**
 * This job manages reading and processing the snapshots
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class DataPrepareJob extends StoppableJob {

	private final ReaderJob readerJob;
	private final ExecutorService executorService;
	private final Logger logger = Logger.getLogger(DataPrepareJob.class);
	private final Set<Entry> destination;

	public DataPrepareJob(ReaderJob readerJob, ExecutorService executorService, Set<Entry> destination) {
		super();
		this.readerJob = readerJob;
		this.executorService = executorService;
		this.destination = destination;
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

				ProcessJob processJob = new ProcessJob(priority, snapshots.getRight());
				Future<Set<Entry>> future = executorService.submit(processJob);

				Set<Entry> result = future.get(3, TimeUnit.SECONDS);
				destination.addAll(result);

			} else {
				logger.info("Job " + readerJob.getClass() + " has finished");
				if (getFuture() != null) {
					logger.info("Trying to stop the job");
					getFuture().cancel(false);
				} else {
					logger.info("Job cannot be stopped - no future object registered");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
