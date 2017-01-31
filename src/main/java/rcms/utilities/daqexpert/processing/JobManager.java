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
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.Entry;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;
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

		Date endDate = null;
		String endDateString = Application.get().getProp(Setting.PROCESSING_END_DATETIME);
		if (endDateString.equalsIgnoreCase("unlimited")) {
			logger.info("Expert run unlimited, will process as long as there are new snapshots");
		} else {
			try {
				endDate = DatatypeConverter.parseDateTime(endDateString).getTime();
			} catch (IllegalArgumentException e) {
				throw new ExpertException(ExpertExceptionCode.CannotParseProcessingEndDate,
						"Cannot parse end date " + endDateString + ", (special key possible 'unlimited')");
			}
		}

		logger.info("Data will be processed from: " + startDate +  (endDate != null? ", to: " + endDate : ""));
		Application.get().getDataManager().setLastUpdate(startDate);
		persistVersion(startDate, endDate);

		mainExecutor = new ThreadPoolExecutor(NUMBER_OF_MAIN_THREADS, NUMBER_OF_MAIN_THREADS, 0L, TimeUnit.MILLISECONDS,
				new PriorityBlockingQueue<Runnable>(INITIAL_QUEUE_SIZE, new PriorityFutureComparator())) {

			protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
				RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
				return new PriorityFuture<T>(newTaskFor, ((ProcessJob) callable).getPriority());
			}
		};

		PersistenceExplorer persistenceExplorer = new PersistenceExplorer(new FileSystemConnector());
		onDemandReader = new OnDemandReaderJob(persistenceExplorer, sourceDirectory);
		ForwardReaderJob frj = new ForwardReaderJob(persistenceExplorer, startDate.getTime(),
				endDate != null ? endDate.getTime() : null, sourceDirectory);

		EventProducer eventProducer = new EventProducer();
		SnapshotProcessor snapshotProcessor = new SnapshotProcessor(eventProducer);

		futureDataPrepareJob = new DataPrepareJob(frj, mainExecutor, dataManager, snapshotProcessor,
				persistenceManager);

		readerRaskController = new JobScheduler(futureDataPrepareJob);
	}

	private void persistVersion(Date startDate, Date endDate) {

		Entry entry = new Entry();
		entry.setStart(startDate);
		entry.setEnd(endDate);
		if (endDate != null) {
			entry.calculateDuration();
		}
		// TODO: class name vs priority - decide on one convention
		entry.setClassName(EventPriority.DEFAULTT.getCode());
		entry.setGroup(EventGroup.EXPERT_VERSION.getCode());
		entry.setContent(this.getClass().getPackage().getImplementationVersion());
		this.persistenceManager.persist(entry);
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
