package rcms.utilities.daqexpert;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class ReadTaskController {

	private static final int PAST_TASK_PERION_IN_SECONDS = 15;
	private static final int REAL_TIME_TASK_PERION_IN_SECONDS = 3;

	private static final Logger logger = Logger.getLogger(ReadTaskController.class);

	private long startTime;

	public ReadTaskController() {
		//bias for generation of events.
		startTime = (new Date()).getTime() - 1000*60*60*5;
	}

	private final ScheduledExecutorService pastReaderScheduler = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService realTimeScheduler = Executors.newScheduledThreadPool(1);

	public void firePastReaderTask(String sourceDirectory) {
		logger.info("Starting past reader task with period of " + PAST_TASK_PERION_IN_SECONDS + " seconds");

		ReaderTask task = new PastReader(new DataResolutionManager(), sourceDirectory, 1471392000000L, startTime);

		ScheduledFuture<?> future = pastReaderScheduler.scheduleAtFixedRate(task, 15, 15, SECONDS);
		task.future = future;

	}

	public void fireRealTimeReaderTask(String sourceDirectory) {
		logger.info("Starting RT reader task with period of " + REAL_TIME_TASK_PERION_IN_SECONDS + " seconds");
		realTimeScheduler.scheduleAtFixedRate(new ForwardReaderTask(new DataResolutionManager(),  sourceDirectory,startTime), 1,
				REAL_TIME_TASK_PERION_IN_SECONDS, SECONDS);

	}
}