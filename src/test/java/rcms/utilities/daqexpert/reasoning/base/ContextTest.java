package rcms.utilities.daqexpert.reasoning.base;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqexpert.processing.context.ContextHandler;

public class ContextTest {

	@Test
	public void risingAverageGenerateSignalTest() {
		ContextHandler c = new ContextHandler();
		c.setHighlightMarkup(false);

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
		ContextHandler c = new ContextHandler();
		c.setHighlightMarkup(false);

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
		ContextHandler c = new ContextHandler();
		c.setHighlightMarkup(false);

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

	@Test
	public void resettingTest(){
		ContextHandler c = new ContextHandler();
		c.setHighlightMarkup(false);

		c.registerForStatistics("x",1);
		c.registerForStatistics("x",2);
		c.registerForStatistics("x",3);
		Assert.assertEquals("( last: 3,  avg: 2,  min: 1,  max: 3)",c.putContext("{{x}}"));


		c.clearContext();

		c.registerForStatistics("x",4);
		c.registerForStatistics("x",5);
		c.registerForStatistics("x",6);
		Assert.assertEquals("( last: 6,  avg: 5,  min: 4,  max: 6)",c.putContext("{{x}}"));




	}

}
