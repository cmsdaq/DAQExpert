package rcms.utilities.daqexpert.reasoning.base;

import org.junit.Assert;
import org.junit.Test;

public class ContextTest {

	@Test
	public void risingAverageGenerateSignalTest() {
		Context c = new Context();

		Assert.assertFalse("First change will not generate anything", c.registerForStatistics("x", 100));
		Assert.assertFalse(c.registerForStatistics("x", 105));
		Assert.assertFalse(c.registerForStatistics("x", 105));

		// not immediately but after it stabilize the notification will be
		// generated
		Assert.assertFalse(c.registerForStatistics("x", 120));
		Assert.assertFalse(c.registerForStatistics("x", 120));
		Assert.assertTrue(c.registerForStatistics("x", 120));

	}

	@Test
	public void fallingAverageGenerateSignalTest() {
		Context c = new Context();

		Assert.assertFalse("First change will not generate anything", c.registerForStatistics("x", 100));
		Assert.assertFalse(c.registerForStatistics("x", 95));
		Assert.assertFalse(c.registerForStatistics("x", 95));

		// not immediately but after it stabilize the notification will be
		// generated
		Assert.assertFalse(c.registerForStatistics("x", 80));
		Assert.assertFalse(c.registerForStatistics("x", 80));
		Assert.assertTrue(c.registerForStatistics("x", 80));

	}

	@Test
	public void secondSignalTest() {
		Context c = new Context();

		Assert.assertFalse("First change will not generate anything", c.registerForStatistics("x", 100));
		Assert.assertFalse(c.registerForStatistics("x", 105));
		Assert.assertFalse(c.registerForStatistics("x", 105));

		// this will generate
		Assert.assertTrue(c.registerForStatistics("x", 200));

		// now the average is 127 and it will take some time to reach next
		// threshold which is ~140 with following values
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertFalse(c.registerForStatistics("x", 145));
		Assert.assertTrue(c.registerForStatistics("x", 145));

	}

}
