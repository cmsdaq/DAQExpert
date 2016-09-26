package rcms.utilities.daqexpert.segmentation;

import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqexpert.TestDummyDAQFactory;

/**
 * Update the raw data and observe the limited resolution streams done by
 * {@link DataResolutionManager}
 */
public class DataResolutionManagerTest {

	int counter = 1;
	DataResolutionManager drm = new DataResolutionManager();

	public void addRandomAndExpect(Boolean minute, Boolean hour, Boolean day, Boolean month) {
		Map<Resolution, Boolean> a = drm.queue(TestDummyDAQFactory.of(counter * 1000, counter, counter * 10));

		if (minute != null)
			Assert.assertEquals(minute, a.get(Resolution.Minute));

		if (hour != null)
			Assert.assertEquals(hour, a.get(Resolution.Hour));

		if (day != null)
			Assert.assertEquals(day, a.get(Resolution.Day));

		if (month != null)
			Assert.assertEquals(month, a.get(Resolution.Month));
		counter++;
	}

	@Test
	public void minuteTest() {
		int threshold = 99;
		drm = new DataResolutionManager();
		addRandomAndExpect(false, null, null, null);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(false, null, null, null);
		}
		addRandomAndExpect(true, null, null, null);
		addRandomAndExpect(false, null, null, null);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(false, null, null, null);
		}
		addRandomAndExpect(true, null, null, null);
	}

	@Test
	public void hourTest() {
		int threshold = 999;
		drm = new DataResolutionManager();
		addRandomAndExpect(null, false, null, null);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(null, false, null, null);
		}
		addRandomAndExpect(null, true, null, null);
		addRandomAndExpect(null, false, null, null);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(null, false, null, null);
		}
		addRandomAndExpect(null, true, null, null);
	}

	@Test
	public void dayTest() {
		int threshold = 9999;
		drm = new DataResolutionManager();
		addRandomAndExpect(null, null, false, null);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(null, null, false, null);
		}
		addRandomAndExpect(null, null, true, null);
		addRandomAndExpect(null, null, false, null);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(null, null, false, null);
		}
		addRandomAndExpect(null, null, true, null);
	}

	@Test
	@Ignore // tests takes too long
	public void monthTest() {
		int threshold = 99999;
		drm = new DataResolutionManager();
		addRandomAndExpect(null, null, null, false);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(null, null, null, false);
		}
		addRandomAndExpect(null, null, null, true);
	}

}
