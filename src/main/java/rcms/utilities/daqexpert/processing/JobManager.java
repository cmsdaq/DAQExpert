package rcms.utilities.daqexpert.processing;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.Entry;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
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

	private final PersistenceManager persistenceManager;

	private final ThreadPoolExecutor mainExecutor;

	private final OnDemandReaderJob onDemandReader;

	private final DataPrepareJob futureDataPrepareJob;

	private final JobScheduler readerRaskController;

	public JobManager(String sourceDirectory, DataManager dataManager) {

		this.persistenceManager = Application.get().getPersistenceManager();

		Date startDate = DatatypeConverter.parseDateTime(Application.get().getProp(Setting.PROCESSING_START_DATETIME))
				.getTime();
		Date endDate = DatatypeConverter.parseDateTime(Application.get().getProp(Setting.PROCESSING_END_DATETIME))
				.getTime();

		logger.info("Data will be processed from: " + startDate + ", to: " + endDate);
		Application.get().getDataManager().setLastUpdate(startDate);

		mainExecutor = new ThreadPoolExecutor(NUMBER_OF_MAIN_THREADS, NUMBER_OF_MAIN_THREADS, 0L, TimeUnit.MILLISECONDS,
				new PriorityBlockingQueue<Runnable>(INITIAL_QUEUE_SIZE, new PriorityFutureComparator())) {

			protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
				RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
				return new PriorityFuture<T>(newTaskFor, ((ProcessJob) callable).getPriority());
			}
		};

		PersistenceExplorer persistenceExplorer = new PersistenceExplorer(new FileSystemConnector());
		onDemandReader = new OnDemandReaderJob(persistenceExplorer, sourceDirectory);
		ForwardReaderJob frj = new ForwardReaderJob(persistenceExplorer, startDate.getTime(), endDate.getTime(), sourceDirectory);

		EventProducer eventProducer = new EventProducer();
		SnapshotProcessor snapshotProcessor = new SnapshotProcessor(eventProducer);

		futureDataPrepareJob = new DataPrepareJob(frj, mainExecutor, dataManager, snapshotProcessor,
				persistenceManager);

		readerRaskController = new JobScheduler(futureDataPrepareJob);
	}

	public void startJobs() {
		readerRaskController.fireRealTimeReaderTask();
	}

	public Future fireOnDemandJob(long startTime, long endTime, Set<Entry> destination, String scriptName) {

		EventProducer eventProducer = new EventProducer();
		SnapshotProcessor snapshotProcessor2 = new SnapshotProcessor(eventProducer);
		DataPrepareJob onDemandDataJob = new DataPrepareJob(onDemandReader, mainExecutor, null, snapshotProcessor2,
				persistenceManager);
		onDemandReader.setTimeSpan(startTime, endTime);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().getExperimentalProcessor()
				.setRequestedScript(scriptName);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().setArtificialForced(true);
		onDemandDataJob.getSnapshotProcessor().clearProducer();
		return readerRaskController.scheduleOnDemandReaderTask(onDemandDataJob);
	}
}
