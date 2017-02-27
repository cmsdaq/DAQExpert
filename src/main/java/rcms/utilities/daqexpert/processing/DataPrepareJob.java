package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.events.Event;
import rcms.utilities.daqexpert.events.EventRegister;
import rcms.utilities.daqexpert.events.EventSender;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;

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

	public DataPrepareJob(ReaderJob readerJob, ExecutorService executorService, DataManager dataManager,
			SnapshotProcessor snapshotProcessor, PersistenceManager persistenceManager, EventRegister eventRegister,
			EventSender eventSender) {
		super();
		this.readerJob = readerJob;
		this.executorService = executorService;
		this.dataManager = dataManager;
		this.snapshotProcessor = snapshotProcessor;
		this.persistenceManager = persistenceManager;
		this.eventRegister = eventRegister;
		this.eventSender = eventSender;
	}

	private static int priority = 0;

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

					try {

						long t1 = System.currentTimeMillis();
						persistenceManager.persist(result.getLeft());
						long t2 = System.currentTimeMillis();
						persistenceManager.persist(result.getRight());
						long t3 = System.currentTimeMillis();

						logger.info("Persistence finished in: " + (t3 - t1) + "ms, " + result.getLeft().size()
								+ " entries in: " + (t2 - t1) + "ms , " + result.getRight().size() + " points in: "
								+ (t3 - t2) + "ms");

						int success = 0;
						int failed = 0;
						for (Event event : eventRegister.getEvents()) {
							boolean successful = eventSender.send(event.generateEventToSend());
							if (!successful) {
								logger.error("Problem sending to nm : " + event.generateEventToSend().toString());
								failed++;
							} else {
								success++;
							}
						}
						eventRegister.getEvents().clear();
						if (failed != 0) {
							logger.warn(failed + " events failed to send, " + success + " successful");
						} else if (success != 0) {
							logger.info("All " + success + " events successfully sent to nm");
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
