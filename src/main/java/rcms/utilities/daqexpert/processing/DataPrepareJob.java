package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.ConditionEventResource;
import rcms.utilities.daqexpert.events.EventRegister;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.websocket.ConditionDashboard;

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
	private DataManager dataManager;
	private final PersistenceManager persistenceManager;

	private final SnapshotProcessor snapshotProcessor;

	private final EventRegister eventRegister;
	private final EventSender eventSender;

	private final ConditionDashboard conditionDashboard;

	/** flag to do demo run */
	private final boolean demoRun;

	public DataPrepareJob(ReaderJob readerJob, ExecutorService executorService, DataManager dataManager,
			SnapshotProcessor snapshotProcessor, PersistenceManager persistenceManager, EventRegister eventRegister,
			EventSender eventSender, ConditionDashboard conditionDashboard, boolean demoRun) {
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

					if (priority == Integer.MAX_VALUE)
						priority = 0;
					else
						priority++;

					ProcessJob snapshotRetrieveAndAnalyzeJob = new ProcessJob(priority, snapshots.getRight(),
							dataManager, snapshotProcessor);
					Future<Pair<Set<Condition>, List<Point>>> future = executorService
							.submit(snapshotRetrieveAndAnalyzeJob);

					Pair<Set<Condition>, List<Point>> result = future.get(10, TimeUnit.SECONDS);

					if (result == null) {
						logger.info("No result this round");
						return;
					}

					try {

						long t1 = System.currentTimeMillis();
						persistenceManager.persist(result.getLeft());
						long t2 = System.currentTimeMillis();
						persistenceManager.persist(result.getRight());
						long t3 = System.currentTimeMillis();

						logger.debug("Persistence finished in: " + (t3 - t1) + "ms, " + result.getLeft().size()
								+ " entries in: " + (t2 - t1) + "ms , " + result.getRight().size() + " points in: "
								+ (t3 - t2) + "ms");

						conditionDashboard.update(result.getLeft());

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
							logger.info(sent + " events sucessfully sent to NotificationManager");
							eventRegister.getEvents().clear();
						}

					} catch (RuntimeException e) {
						logger.warn("Exception during result persistence - results will be forgotten");
						logger.error(e);
						e.printStackTrace();
					}
				}

			}

		} catch (Exception e) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, e.getMessage());
		}

	}

	protected SnapshotProcessor getSnapshotProcessor() {
		return snapshotProcessor;
	}

}
