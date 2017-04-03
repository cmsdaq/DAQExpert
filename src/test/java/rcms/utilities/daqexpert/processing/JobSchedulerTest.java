package rcms.utilities.daqexpert.processing;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jmock.lib.concurrent.DeterministicScheduler;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JobSchedulerTest {

	private static final Logger logger = Logger.getLogger(JobScheduler.class);

	Runnable simplePastTask = new Runnable() {

		@Override
		public void run() {
			logger.debug("P");
			counter += 10;
		}
	};

	Runnable simpleRTTask = new Runnable() {

		@Override
		public void run() {
			logger.debug("RT");
			counter += 1;
		}
	};

	private static int counter;

	@Test
	public void realTimeSchedulingTest() throws InterruptedException {

		counter = 0;
		DeterministicScheduler s1 = new DeterministicScheduler();
		JobScheduler dpc = new JobScheduler(simpleRTTask, s1, null, 2000);

		dpc.fireRealTimeReaderTask();

		s1.tick(1, TimeUnit.SECONDS);
		Assert.assertEquals(1, counter);
		s1.tick(1, TimeUnit.SECONDS);
		Assert.assertEquals(1, counter);
		s1.tick(1, TimeUnit.SECONDS);
		Assert.assertEquals(2, counter);
		s1.tick(1, TimeUnit.SECONDS);
		Assert.assertEquals(2, counter);
		s1.tick(1, TimeUnit.SECONDS);
		Assert.assertEquals(3, counter);
		s1.tick(1, TimeUnit.SECONDS);
		Assert.assertEquals(3, counter);
		s1.tick(2, TimeUnit.SECONDS);
		Assert.assertEquals(4, counter);
		s1.tick(2, TimeUnit.SECONDS);
		Assert.assertEquals(5, counter);
		s1.tick(2, TimeUnit.SECONDS);
		Assert.assertEquals(6, counter);
	}

	@Test
	@Ignore // past data became on demand data - test to be rewritten
	public void pastDataSchedulingTest() throws InterruptedException {

		counter = 0;
		DeterministicScheduler s1 = new DeterministicScheduler();
		JobScheduler dpc = new JobScheduler(simpleRTTask, null, s1, 2000);

		dpc.scheduleOnDemandReaderTask(simplePastTask);

		s1.tick(5, TimeUnit.SECONDS);
		Assert.assertEquals(0, counter);
		s1.tick(5, TimeUnit.SECONDS);
		Assert.assertEquals(0, counter);
		s1.tick(5, TimeUnit.SECONDS);
		Assert.assertEquals(10, counter);
		s1.tick(5, TimeUnit.SECONDS);
		Assert.assertEquals(10, counter);
		s1.tick(5, TimeUnit.SECONDS);
		Assert.assertEquals(10, counter);
		s1.tick(5, TimeUnit.SECONDS);
		Assert.assertEquals(20, counter);
		s1.tick(15, TimeUnit.SECONDS);
		Assert.assertEquals(30, counter);
		s1.tick(15, TimeUnit.SECONDS);
		Assert.assertEquals(40, counter);
		s1.tick(150, TimeUnit.SECONDS);
		Assert.assertEquals(140, counter);
	}

}