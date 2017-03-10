package rcms.utilities.daqexpert.processing;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	private static final int REAL_TIME_TASK_PERION_IN_MILLISECONDS = 2000;

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
		logger.info("Starting RT reader task with period of " + REAL_TIME_TASK_PERION_IN_MILLISECONDS + " seconds");
		realTimeScheduler.scheduleAtFixedRate(realTimeTask, 1, REAL_TIME_TASK_PERION_IN_MILLISECONDS , TimeUnit.MILLISECONDS);
	}

	public void stopExecutors() {
		onDemandScheduler.shutdown();
		realTimeScheduler.shutdown();
		try {
			onDemandScheduler.awaitTermination(5, SECONDS);
			logger.info("Successfully terminated on-demand scheduler");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			realTimeScheduler.awaitTermination(5, SECONDS);
			logger.info("Successfully terminated real-time scheduler");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}