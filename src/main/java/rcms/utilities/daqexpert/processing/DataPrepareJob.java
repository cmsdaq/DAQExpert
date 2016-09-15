package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

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

	public DataPrepareJob(ReaderJob readerJob, ExecutorService executorService) {
		super();
		this.readerJob = readerJob;
		this.executorService = executorService;
	}

	@Override
	public void run() {

		try {
			Pair<Long, List<File>> result;

			if (!readerJob.finished()) {
				result = readerJob.read();

				ProcessJob processJob = new ProcessJob(1, result.getRight());
				executorService.submit(processJob);
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
