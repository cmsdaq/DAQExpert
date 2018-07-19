package rcms.utilities.daqexpert.processing;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.ConditionEventResource;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.events.collectors.EventRegister;
import rcms.utilities.daqexpert.jobs.RecoveryJobManager;
import rcms.utilities.daqexpert.jobs.RecoveryRequest;
import rcms.utilities.daqexpert.jobs.RecoveryRequestBuilder;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.DominatingPersistor;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;
import rcms.utilities.daqexpert.reasoning.causality.DominatingSelector;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.websocket.ConditionDashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This job manages reading and processing the snapshots
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 * TODO: more appropriate name for this class is SnapshotProcessJobBuilder
 */
public class DataPrepareJob implements Runnable {

	private final ReaderJob readerJob;
	private final ExecutorService executorService;
	private final Logger logger = Logger.getLogger(DataPrepareJob.class);
	private final RecoveryJobManager recoveryJobManager;
	private DataManager dataManager;
	private final PersistenceManager persistenceManager;

	private final SnapshotProcessor snapshotProcessor;

	private final EventRegister eventRegister;
	protected final EventSender eventSender;

	private final ConditionDashboard conditionDashboard;

	private final DominatingPersistor dominatingPersistor;

	private final DominatingSelector dominatingSelector;

	/** flag to do demo run */
	private final boolean demoRun;

	private boolean waiting;

	private static Condition lastDominating;

	/** List of conditions that has been started - kept in order to generate finish signals for controller */
	private static final List<Long> recoveryConditionIds = new ArrayList<>();

	public boolean isWaiting(){
		return waiting;
	}

	public DataPrepareJob(ReaderJob readerJob, ExecutorService executorService, DataManager dataManager,
			SnapshotProcessor snapshotProcessor, PersistenceManager persistenceManager, EventRegister eventRegister,
			EventSender eventSender, ConditionDashboard conditionDashboard, RecoveryJobManager recoveryJobManager, boolean demoRun, DominatingPersistor dominatingPersistor) {
		super();
		this.readerJob = readerJob;
		this.executorService = executorService;
		this.dataManager = dataManager;
		this.snapshotProcessor = snapshotProcessor;
		this.persistenceManager = persistenceManager;
		this.eventRegister = eventRegister;
		this.eventSender = eventSender;
		this.conditionDashboard = conditionDashboard;
		this.demoRun = demoRun;
		this.dominatingPersistor = dominatingPersistor;
		this.dominatingSelector = new DominatingSelector();
		this.recoveryJobManager = recoveryJobManager;
		ProcessJob.flush();
	}

	private static int priority = 0;

	// TODO: Delememe
	private static Long id = 0L;

	@Override
	public void run() {

		try {
			Pair<Long, List<File>> snapshots;

			if (!readerJob.finished()) {
				snapshots = readerJob.read();

				if (snapshots.getRight().size() > 0) {
					waiting = false;

					logger.debug("Processing " + snapshots.getRight().size() + " this round");

					if (priority == Integer.MAX_VALUE)
						priority = 0;
					else
						priority++;

					ProcessJob snapshotRetrieveAndAnalyzeJob = new ProcessJob(priority, snapshots.getRight(),
							dataManager, snapshotProcessor, recoveryJobManager, dominatingSelector, dominatingPersistor);
					Future<Triple<Set<Condition>, List<Point>, Condition>> future = executorService
							.submit(snapshotRetrieveAndAnalyzeJob);

					Triple<Set<Condition>, List<Point>, Condition> result = future.get(10, TimeUnit.SECONDS);


					if (result == null) {
						logger.info("No result this round");
						return;
					}

					Condition dominating = result.getRight();


					try {

						long t1 = System.currentTimeMillis();
						persistenceManager.persist(result.getLeft());
						long t2 = System.currentTimeMillis();
						persistenceManager.persist(result.getMiddle());
						long t3 = System.currentTimeMillis();

						logger.debug("Persistence finished in: " + (t3 - t1) + "ms, " + result.getLeft().size()
								+ " entries in: " + (t2 - t1) + "ms , " + result.getMiddle().size() + " points in: "
								+ (t3 - t2) + "ms");


						conditionDashboard.update(result.getLeft(), result.getRight() != null? result.getRight().getId(): null);


						if(dominating != lastDominating){
							handleController(result.getLeft(), dominating);
						}

						if (demoRun && conditionDashboard.getCurrentCondition() != null
								&& id != conditionDashboard.getCurrentCondition().getId()) {
							Thread.sleep(2000);
							id = conditionDashboard.getCurrentCondition().getId();
						}

						logger.debug(conditionDashboard.toString());

						if (eventRegister.getEvents().size() > 0) {
							List<ConditionEventResource> eventsToSend = new ArrayList<>();
							for (ConditionEvent conditionEvent : eventRegister.getEvents()) {
								eventsToSend.add(conditionEvent.generateEventToSend());
							}
							int sent = eventSender.sendBatchEvents(eventsToSend);
							logger.info(sent + " events successfully sent to NotificationManager");
							eventRegister.getEvents().clear();
						}

						lastDominating = dominating;

					} catch (RuntimeException e) {
						logger.warn("Exception during result persistence - results will be forgotten");
						logger.error(e);
						e.printStackTrace();
					}
				} else{

					waiting=true;
				}

			}

		} catch (Exception e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, e.getMessage());
		}

	}

	protected SnapshotProcessor getSnapshotProcessor() {
		return snapshotProcessor;
	}

	/**
	 *
	 * @param conditions conditions generated this round
	 * @param dominating current active dominating condition
	 */
	private void handleController(Set<Condition> conditions, Condition dominating ){
		// find the conditions that finished - send the 'finish' signals to Controller
		for(Condition logicResult: conditions) {
			if (logicResult.getState() == EntryState.FINISHED) {
				if (recoveryConditionIds.contains(logicResult.getId())) {
					Long id = logicResult.getId();
					logger.info("Notifying controller condition " + id + "  finished");
					recoveryJobManager.notifyConditionFinished(id);
					recoveryConditionIds.remove(id);
				}
				continue;
			}
		}

		if (dominating != null && dominating.getProducer() instanceof ActionLogicModule) {


			logger.info("Dominating problem has recovery steps: " + dominating.getActionSteps());
			logger.info("Trying to delegate to controller");
			ActionLogicModule actionDominating = (ActionLogicModule) dominating.getProducer();
			RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
			RecoveryRequest recoveryRequest = recoveryRequestBuilder.buildRecoveryRequest(
					actionDominating.getActionWithContextRawRecovery(),
					actionDominating.getActionWithContext(),
					actionDominating.getName(),
					actionDominating.getDescriptionWithContext(),
					dominating.getId());

			if(recoveryRequest != null && recoveryRequest.getRecoverySteps().size() > 0) {
				recoveryRequest.setCondition(dominating);
				Long dominatingId = recoveryJobManager.runRecoveryJob(recoveryRequest);
				if (dominatingId != null) {
					recoveryConditionIds.add(dominatingId);
					logger.info("Automatic recovery sent to the controller. Executable steps: " + recoveryRequest.getRecoverySteps());
				}
			} else{
				logger.info("Recovery request was not build. No recovery steps that could be executed.");
			}
		}


	}
}
