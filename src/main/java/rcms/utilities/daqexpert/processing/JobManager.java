package rcms.utilities.daqexpert.processing;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;

/**
 * Manages the jobs of retrieving and processing the data (snapshots)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class JobManager {

	private static final Logger logger = Logger.getLogger(JobManager.class);
	/**
	 * Number of main threads - should be 1 so that there is not concurrent
	 * access to analysis stream
	 */
	private static final int NUMBER_OF_MAIN_THREADS = 1;

	/** Initial queue size */
	private static final int INITIAL_QUEUE_SIZE = 3;

	private final ThreadPoolExecutor mainExecutor;

	private final DataPrepareJob pastDataPrepareJob;

	private final DataPrepareJob futureDataPrepareJob;

	public JobManager(String sourceDirectory) {

		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int offset = 1000 * 60 * 60;// * 24 * 2;
		long startTime = utcCalendar.getTimeInMillis() - offset;
		logger.info("Data will be processed from: " + utcCalendar.getTime() + " minus offset of " + offset + "ms");

		mainExecutor = new ThreadPoolExecutor(NUMBER_OF_MAIN_THREADS, NUMBER_OF_MAIN_THREADS, 0L, TimeUnit.MILLISECONDS,
				new PriorityBlockingQueue<Runnable>(INITIAL_QUEUE_SIZE, new PriorityFutureComparator())) {

			protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
				RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
				return new PriorityFuture<T>(newTaskFor, ((ProcessJob) callable).getPriority());
			}
		};

		PersistenceExplorer persistenceExplorer = new PersistenceExplorer(new FileSystemConnector());
		PastReaderJob prj = new PastReaderJob(persistenceExplorer, sourceDirectory, 1471392000000L, startTime);
		ForwardReaderJob frj = new ForwardReaderJob(persistenceExplorer, startTime, sourceDirectory);

		pastDataPrepareJob = new DataPrepareJob(prj, mainExecutor);
		futureDataPrepareJob = new DataPrepareJob(frj, mainExecutor);
	}

	public void startJobs() {

		JobScheduler readerRaskController = new JobScheduler(pastDataPrepareJob, futureDataPrepareJob);
		// readerRaskController.firePastReaderTask();
		readerRaskController.fireRealTimeReaderTask();
	}
}
