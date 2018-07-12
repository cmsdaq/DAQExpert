package rcms.utilities.daqexpert.processing;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.events.collectors.EventPrinter;
import rcms.utilities.daqexpert.events.collectors.EventRegister;
import rcms.utilities.daqexpert.events.collectors.MatureEventCollector;
import rcms.utilities.daqexpert.jobs.RecoveryJobManager;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.DominatingPersistor;
import rcms.utilities.daqexpert.persistence.InternalConditionProducer;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.processing.ConditionProducer;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.websocket.ConditionDashboard;

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

	protected final DataPrepareJob futureDataPrepareJob;

	private final JobScheduler readerRaskController;

	private final ConditionProducer eventProducer;

	private Condition versionCondition;
	
	protected final EventSender eventSender;

	private final CleanStartupVerifier cleanStartupVerifier;

	private final DominatingPersistor dominatingPersistor;

	private final InternalConditionProducer internalConditionProducer;

	public JobManager(String sourceDirectory, DataManager dataManager, EventSender eventSender, CleanStartupVerifier cleanStartupVerifier, RecoveryJobManager recoveryJobManager) {
		
		this.eventSender = eventSender;
		this.cleanStartupVerifier = cleanStartupVerifier;

		int realTimeReaderPeriod = 2000;
		int batchSnapshotRead = 2000;

		boolean demo = false;
		int demoPeriod = 10;
		if (Application.get().getProp().containsKey("demo")) {
			try {
				Object a = Application.get().getProp().get("demo");
				demo = Boolean.parseBoolean((String) a);

			} catch (NumberFormatException e) {
				logger.warn("Demo configuration could not be parsed");
				e.printStackTrace();
			}
		}
		if (Application.get().getProp().containsKey("demo.delay")) {
			try {
				Object a = Application.get().getProp().get("demo.delay");
				demoPeriod = Integer.parseInt((String) a);

			} catch (NumberFormatException e) {
				logger.warn("Demo configuration could not be parsed");
			}
		}

		if (demo) {
			logger.info("Running in demo mode");
			realTimeReaderPeriod = demoPeriod;
			batchSnapshotRead = 1;
		}

		this.persistenceManager = Application.get().getPersistenceManager();

		this.dominatingPersistor = new DominatingPersistor(persistenceManager);;

		this.internalConditionProducer = new InternalConditionProducer(persistenceManager);

		RunConfigurator runConfigurator = new RunConfigurator(persistenceManager);

		Date startDate = runConfigurator.getStartDate();
		Date endDate = runConfigurator.getEndDate();

		logger.info("Data will be processed from: " + startDate + (endDate != null ? ", to: " + endDate : ""));
		Application.get().getDataManager().setLastUpdate(startDate);

		cleanStartupVerifier.ensureSafeStartupProcedure();
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
				endDate != null ? endDate.getTime() : null, sourceDirectory, batchSnapshotRead);

		eventProducer = new ConditionProducer();

		EventRegister eventRegister = new MatureEventCollector();
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


		Long startTimestampToGenerateNotifications = System.currentTimeMillis() - offset;

		ConditionDashboard conditionDashboard = Application.get().getDashboard();

		futureDataPrepareJob = new DataPrepareJob(frj, mainExecutor, dataManager, snapshotProcessor, persistenceManager,
				eventRegister, eventSender, conditionDashboard, recoveryJobManager, demo, dominatingPersistor);

		readerRaskController = new JobScheduler(futureDataPrepareJob, realTimeReaderPeriod);

		getRecentSuggestions();
	}



	private void persistVersion(Date startDate, Date endDate) {
		String version = this.getClass().getPackage().getImplementationVersion();
		if (version == null) {
			logger.info("Problem detecting version");
			version = "unknown";
		}
		versionCondition = internalConditionProducer.persistCondition(version,startDate,endDate,ConditionGroup.EXPERT_VERSION);
	}

	private void getRecentSuggestions() {
		List<Condition> briefHistory = persistenceManager.getLastActionConditions();

		if (briefHistory != null) {
			logger.info("Getting some conditions from last expert run: " + briefHistory.size());

			Collections.reverse(briefHistory);
			for (Condition condition : briefHistory) {
				Set<Condition> fakeGroup = new HashSet<>();
				fakeGroup.add(condition);
				Application.get().getDashboard().update(fakeGroup, null, false);
			}
		}
	}

	public void startJobs() {
		readerRaskController.fireRealTimeReaderTask();
	}

	public boolean working(){
		return !futureDataPrepareJob.isWaiting();
	}

	public Future fireOnDemandJob(long startTime, long endTime, Set<Condition> destination, String scriptName) {

		ConditionDashboard conditionDashboard = Application.get().getDashboard();
		ConditionProducer conditionProducer = new ConditionProducer();
		EventRegister eventRegister = new EventPrinter();
		conditionProducer.setEventRegister(eventRegister);
		SnapshotProcessor snapshotProcessor2 = new SnapshotProcessor(conditionProducer);
		DataPrepareJob onDemandDataJob = new DataPrepareJob(onDemandReader, mainExecutor, null, snapshotProcessor2,
				persistenceManager, eventRegister, null, conditionDashboard, null,false, null);
		onDemandReader.setTimeSpan(startTime, endTime);
		onDemandDataJob.getSnapshotProcessor().getCheckManager().getExperimentalProcessor()
				.setRequestedScript(scriptName);
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
		internalConditionProducer.updateCondition(versionCondition);

		dominatingPersistor.onExit();

	}

	public ConditionProducer getEventProducer() {
		return eventProducer;
	}
}
