package rcms.utilities.daqexpert.processing;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

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

	/** Executor of on demand reader task */
	private final ExecutorService onDemandScheduler;

	private Runnable realTimeTask;

	/** Scheduled executor of real time reader task */
	private final ScheduledExecutorService realTimeScheduler;

	private static final Logger logger = Logger.getLogger(JobScheduler.class);

	public JobScheduler(Runnable realTimeTask) {
		this(realTimeTask, Executors.newScheduledThreadPool(1), Executors.newFixedThreadPool(1));
	}

	public JobScheduler(Runnable realTimeTask, ScheduledExecutorService realTimeScheduler,
			ExecutorService onDemandScheduler) {

		this.onDemandScheduler = onDemandScheduler;
		this.realTimeScheduler = realTimeScheduler;

		this.realTimeTask = realTimeTask;

	}

	public Future scheduleOnDemandReaderTask(Runnable onDemandReaderTask) {

		logger.info("Starting on-demand reader job");

		Future<?> future = onDemandScheduler.submit(onDemandReaderTask);
		return future;

	}

	public void fireRealTimeReaderTask() {
		logger.info("Starting RT reader task with period of " + REAL_TIME_TASK_PERION_IN_SECONDS + " seconds");
		realTimeScheduler.scheduleAtFixedRate(realTimeTask, 1, REAL_TIME_TASK_PERION_IN_SECONDS, SECONDS);
	}

}