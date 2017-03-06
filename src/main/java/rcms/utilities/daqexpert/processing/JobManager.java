package rcms.utilities.daqexpert.processing;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.events.EventCollector;
import rcms.utilities.daqexpert.events.EventPrinter;
import rcms.utilities.daqexpert.events.EventRegister;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.processing.ConditionProducer;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.websocket.ConditionWebSocketServer;

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

	private final ConditionProducer eventProducer;

	private Condition versionCondition;

	public JobManager(String sourceDirectory, DataManager dataManager) {

		this.persistenceManager = Application.get().getPersistenceManager();

		RunConfigurator runConfigurator = new RunConfigurator(persistenceManager);

		Date startDate = runConfigurator.getStartDate();
		Date endDate = runConfigurator.getEndDate();

		logger.info("Data will be processed from: " + startDate + (endDate != null ? ", to: " + endDate : ""));
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

		eventProducer = new ConditionProducer();

		EventRegister eventRegister = new EventCollector();
		eventProducer.setEventRegister(eventRegister);
		SnapshotProcessor snapshotProcessor = new SnapshotProcessor(eventProducer);

		long offset = 0;
		try {
			offset = Long.parseLong(Application.get().getProp(Setting.NM_OFFSET));
		} catch (NumberFormatException e) {
			logger.error("Problem parsing offset");
		}
		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long startTime = utcCalendar.getTimeInMillis() - offset;
		utcCalendar.setTimeInMillis(startTime);
		Date nmStartDate = utcCalendar.getTime();
		String offsetString = DurationFormatUtils.formatDuration(offset, "d 'days', HH:mm:ss", true);
		logger.info(
				"Notifications will generated from: " + nmStartDate + " (now minus offset of " + offsetString + ")");

		EventSender eventSender = new EventSender(Application.get().getProp(Setting.NM_API_CREATE));

		Long startTimestampToGenerateNotifications = System.currentTimeMillis() - offset;

		futureDataPrepareJob = new DataPrepareJob(frj, mainExecutor, dataManager, snapshotProcessor, persistenceManager,
				eventRegister, eventSender);

		readerRaskController = new JobScheduler(futureDataPrepareJob);

		getRecentSuggestions();
	}

	private void persistVersion(Date startDate, Date endDate) {

		versionCondition = new Condition();
		versionCondition.setStart(startDate);
		versionCondition.setEnd(endDate);
		if (endDate != null) {
			versionCondition.calculateDuration();
		}
		// TODO: class name vs priority - decide on one convention
		versionCondition.setClassName(ConditionPriority.DEFAULTT);

		versionCondition.setGroup(ConditionGroup.EXPERT_VERSION);
		String version = this.getClass().getPackage().getImplementationVersion();
		if (version == null) {
			logger.info("Problem detecting version");
			version = "unknown";
		}
		versionCondition.setTitle(version);
		this.persistenceManager.persist(versionCondition);
	}

	private void getRecentSuggestions() {
		List<Condition> last = persistenceManager.getLastActionConditions();

		if (last != null) {
			logger.info("Getting some conditions from last expert run: " + last.size());

			Collections.reverse(last);
			for (Condition condition : last) {
				ConditionWebSocketServer.sessionHandler.addCondition(condition);
			}
		}
	}

	public void startJobs() {
		readerRaskController.fireRealTimeReaderTask();
	}

	public Future fireOnDemandJob(long startTime, long endTime, Set<Condition> destination, String scriptName) {

		ConditionProducer conditionProducer = new ConditionProducer();
		EventRegister eventRegister = new EventPrinter();
		conditionProducer.setEventRegister(eventRegister);
		SnapshotProcessor snapshotProcessor2 = new SnapshotProcessor(conditionProducer);
		DataPrepareJob onDemandDataJob = new DataPrepareJob(onDemandReader, mainExecutor, null, snapshotProcessor2,
				persistenceManager, eventRegister, null);
		onDemandReader.setTimeSpan(startTime, endTime);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().getExperimentalProcessor()
				.setRequestedScript(scriptName);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().setArtificialForced(true);
		onDemandDataJob.getSnapshotProcessor().clearProducer();
		return readerRaskController.scheduleOnDemandReaderTask(onDemandDataJob);
	}

	public void stop() {

		readerRaskController.stopExecutors();
		mainExecutor.shutdown();

		try {
			mainExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);

			logger.info("All jobs gracefully terminated");
		} catch (InterruptedException e) {

			logger.error("Could not gracefully terminate jobs");
			logger.error(e);
		}

		logger.info("Temporarly finishing events");
		Set<Condition> finished = eventProducer.finish();
		persistenceManager.persist(finished);
		logger.info("Finished " + finished.size() + " conditions.");

		RunConfigurator runConfigurator = new RunConfigurator(persistenceManager);
		Date endDate = runConfigurator.getEndDate();
		if (endDate == null)
			endDate = new Date();
		versionCondition.setEnd(endDate);
		versionCondition.calculateDuration();
		persistenceManager.persist(versionCondition);

	}
}
