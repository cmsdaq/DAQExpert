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

	/**
	 * Period in which past data will be accessed
	 */
	private static final int PAST_TASK_PERION_IN_SECONDS = 15;

	/** Scheduled executor of past reader task */
	private final ScheduledExecutorService pastReaderScheduler;

	private final StoppableJob pastReaderTask;

	private StoppableJob realTimeTask;

	/** Scheduled executor of real time reader task */
	private final ScheduledExecutorService realTimeScheduler;

	private static final Logger logger = Logger.getLogger(JobScheduler.class);

	public JobScheduler(StoppableJob pastReaderTask, StoppableJob realTimeTask) {
		this(pastReaderTask, realTimeTask, Executors.newScheduledThreadPool(1), Executors.newScheduledThreadPool(1));
	}

	public JobScheduler(StoppableJob pastReaderTask, StoppableJob realTimeTask, ScheduledExecutorService realTimeScheduler,
			ScheduledExecutorService pastReaderScheduler) {

		this.pastReaderScheduler = pastReaderScheduler;
		this.realTimeScheduler = realTimeScheduler;

		this.pastReaderTask = pastReaderTask;

		this.realTimeTask = realTimeTask;

	}

	public void firePastReaderTask() {
		logger.info("Starting past reader task with period of " + PAST_TASK_PERION_IN_SECONDS + " seconds");

		ScheduledFuture<?> future = pastReaderScheduler.scheduleAtFixedRate(pastReaderTask, 15, 15, SECONDS);
		pastReaderTask.setFuture(future);

	}

	public void fireRealTimeReaderTask() {
		logger.info("Starting RT reader task with period of " + REAL_TIME_TASK_PERION_IN_SECONDS + " seconds");
		realTimeScheduler.scheduleAtFixedRate(realTimeTask, 1, REAL_TIME_TASK_PERION_IN_SECONDS, SECONDS);
	}

}