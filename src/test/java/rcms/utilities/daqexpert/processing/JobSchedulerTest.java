package rcms.utilities.daqexpert.processing;

import java.util.concurrent.TimeUnit;

import org.jmock.lib.concurrent.DeterministicScheduler;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JobSchedulerTest {

	StoppableJob simplePastTask = new StoppableJob() {

		@Override
		public void run() {
			System.out.println("P");
			counter += 10;
		}
	};

	StoppableJob simpleRTTask = new StoppableJob() {

		@Override
		public void run() {
			System.out.println("RT");
			counter += 1;
		}
	};

	private static int counter;

	@Test
	public void realTimeSchedulingTest() throws InterruptedException {

		counter = 0;
		DeterministicScheduler s1 = new DeterministicScheduler();
		JobScheduler dpc = new JobScheduler(simplePastTask, simpleRTTask, s1, null);

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
		JobScheduler dpc = new JobScheduler(simplePastTask, simpleRTTask, null, s1);

		dpc.onDemandReaderTask();

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

	class DataProcessingControllerMock extends JobScheduler {

		public DataProcessingControllerMock(StoppableJob r1, StoppableJob r2) {
			super(r1, r2);
		}

	}
}