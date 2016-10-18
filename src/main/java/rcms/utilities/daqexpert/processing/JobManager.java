package rcms.utilities.daqexpert.processing;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.processing.EventProducer;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;

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

	private final OnDemandReaderJob onDemandReader;

	private final DataPrepareJob futureDataPrepareJob;

	private final JobScheduler readerRaskController;

	public JobManager(String sourceDirectory, Set<Entry> destination, DataManager dataManager) {

		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int offset = Integer.parseInt(Application.get().getProp().get(Application.OFFSET).toString());
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
		onDemandReader = new OnDemandReaderJob(persistenceExplorer, sourceDirectory);
		ForwardReaderJob frj = new ForwardReaderJob(persistenceExplorer, startTime, sourceDirectory);

		EventProducer eventProducer = new EventProducer();
		SnapshotProcessor snapshotProcessor = new SnapshotProcessor(eventProducer);

		futureDataPrepareJob = new DataPrepareJob(frj, mainExecutor, destination, dataManager, snapshotProcessor);

		readerRaskController = new JobScheduler(futureDataPrepareJob);
	}

	public void startJobs() {
		readerRaskController.fireRealTimeReaderTask();
	}

	public Future fireOnDemandJob(long startTime, long endTime, Set<Entry> destination, String scriptName) {

		EventProducer eventProducer = new EventProducer();
		SnapshotProcessor snapshotProcessor2 = new SnapshotProcessor(eventProducer);
		DataPrepareJob onDemandDataJob = new DataPrepareJob(onDemandReader, mainExecutor, null, null, snapshotProcessor2);
		onDemandReader.setTimeSpan(startTime, endTime);
		onDemandDataJob.setDestination(destination);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().getExperimentalProcessor().setRequestedScript(scriptName);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().setArtificialForced(true);
		onDemandDataJob.getSnapshotProcessor().clearProducer();
		return readerRaskController.scheduleOnDemandReaderTask(onDemandDataJob);
	}
}
