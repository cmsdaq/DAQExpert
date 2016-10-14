package rcms.utilities.daqexpert.processing;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

/**
 * Scheduler of the jobs retrieving the data
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class JobScheduler {

	/**
	 * Period in which real time data will be accessed
	 */
	private static final int REAL_TIME_TASK_PERION_IN_SECONDS = 2;

	/** Scheduled executor of on demand reader task */
	private final ScheduledExecutorService onDemandScheduler;

	private final StoppableJob onDemandReaderTask;

	private StoppableJob realTimeTask;

	/** Scheduled executor of real time reader task */
	private final ScheduledExecutorService realTimeScheduler;

	private static final Logger logger = Logger.getLogger(JobScheduler.class);

	public JobScheduler(StoppableJob pastReaderTask, StoppableJob realTimeTask) {
		this(pastReaderTask, realTimeTask, Executors.newScheduledThreadPool(1), Executors.newScheduledThreadPool(1));
	}

	public JobScheduler(StoppableJob onDemantTask, StoppableJob realTimeTask,
			ScheduledExecutorService realTimeScheduler, ScheduledExecutorService onDemandScheduler) {

		this.onDemandScheduler = onDemandScheduler;
		this.realTimeScheduler = realTimeScheduler;

		this.onDemandReaderTask = onDemantTask;

		this.realTimeTask = realTimeTask;

	}

	public ScheduledFuture scheduleOnDemandReaderTask() {

		logger.info("Starting on-demand reader job");

		ScheduledFuture<?> future = onDemandScheduler.schedule(onDemandReaderTask, 0, SECONDS);
		onDemandReaderTask.setFuture(future);
		return future;

	}

	public void fireRealTimeReaderTask() {
		logger.info("Starting RT reader task with period of " + REAL_TIME_TASK_PERION_IN_SECONDS + " seconds");
		realTimeScheduler.scheduleAtFixedRate(realTimeTask, 1, REAL_TIME_TASK_PERION_IN_SECONDS, SECONDS);
	}

}