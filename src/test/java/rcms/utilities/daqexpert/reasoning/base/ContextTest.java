package rcms.utilities.daqexpert.reasoning.base;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContextTest {

	Context context;
	String message = "FED(s) {{key}} of ...";

	@Before
	public void init() {
		context = new Context();
	}

	@Test
	public void singleElementInContextTest() {
		context.register("key", "1");
		Assert.assertEquals("FED(s) 1 of ...", context.getContentWithContext(message));
	}

	@Test
	public void littleElementsInContext() {
		context.register("key", "1");
		context.register("key", "2");
		context.register("key", "3");
		context.register("key", "4");
		context.register("key", "5");
		context.register("key", "6");
		context.register("key", "7");
		context.register("key", "8");
		Assert.assertEquals("FED(s) 1, 2, 3, 4, 5, 6, 7, 8 of ...", context.getContentWithContext(message));
	}
	
	@Test
	public void manyElementsInContext() {
		context.register("key", "1");
		context.register("key", "2");
		context.register("key", "3");
		context.register("key", "4");
		context.register("key", "5");
		context.register("key", "6");
		context.register("key", "7");
		context.register("key", "8");
		context.register("key", "9");
		Assert.assertEquals("FED(s) (1, 2, 3, 4, 5, 6, 7 and 2 more) of ...", context.getContentWithContext(message));
	}

}
