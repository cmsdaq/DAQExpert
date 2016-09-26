package rcms.utilities.daqexpert.segmentation;

import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import rcms.utilities.daqexpert.TestDummyDAQFactory;

/**
 * Update the raw data and observe the limited resolution streams done by
 * {@link DataResolutionManager}
 */
public class DataResolutionManagerTest {

	StreamProcessor minuteStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Minute),
			SegmentationSettings.Minute);
	StreamProcessor hourStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Hour),
			SegmentationSettings.Hour);
	StreamProcessor dayStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Day),
			SegmentationSettings.Day);
	StreamProcessor monthStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Month),
			SegmentationSettings.Month);

	int counter = 1;
	DataResolutionManager drm;

	private Map<DataResolution, Boolean> addRandom() {
		return drm.queue(TestDummyDAQFactory.of(counter * 1000, counter, counter * 10));
	}

	private void addRandomAndExpect(Boolean minute, Boolean hour, Boolean day, Boolean month) {
		Map<DataResolution, Boolean> a = addRandom();

		if (minute != null)
			Assert.assertEquals(minute, a.get(DataResolution.Minute));

		if (hour != null)
			Assert.assertEquals(hour, a.get(DataResolution.Hour));

		if (day != null)
			Assert.assertEquals(day, a.get(DataResolution.Day));

		if (month != null)
			Assert.assertEquals(month, a.get(DataResolution.Month));
		counter++;
	}

	@Test
	public void segmentationTriggerForMinuteStreamTest() {
		int threshold = 99;

		drm = new DataResolutionManager(minuteStreamProcessor, hourStreamProcessor, dayStreamProcessor,
				monthStreamProcessor);
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
	public void segmentationTriggerForHourStream() {
		int threshold = 999;
		drm = new DataResolutionManager(minuteStreamProcessor, hourStreamProcessor, dayStreamProcessor,
				monthStreamProcessor);
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
	public void segmentationTriggerForDaqStream() {
		int threshold = 9999;
		drm = new DataResolutionManager(minuteStreamProcessor, hourStreamProcessor, dayStreamProcessor,
				monthStreamProcessor);
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
	public void segmentationTriggerForMonthStream() {
		int threshold = 99999;
		drm = new DataResolutionManager(minuteStreamProcessor, hourStreamProcessor, dayStreamProcessor,
				monthStreamProcessor);
		addRandomAndExpect(null, null, null, false);
		for (int i = 0; i < threshold; i++) {
			addRandomAndExpect(null, null, null, false);
		}
		addRandomAndExpect(null, null, null, true);
	}

	@Test
	public void interactionTest() {

		LinearSegmentator minuteSegmentator = Mockito.mock(LinearSegmentator.class);

		StreamProcessor minuteStreamProcessor = new StreamProcessor(minuteSegmentator, SegmentationSettings.Minute);
		drm = new DataResolutionManager(minuteStreamProcessor, hourStreamProcessor, dayStreamProcessor,
				monthStreamProcessor);
		for (int i = 0; i < 299; i++) {
			addRandom();
		}

		// 2 times (because of 2 streams) of segmentate invocation per number
		// (=threshold) of new data
		Mockito.verify(minuteSegmentator, Mockito.times(4)).segmentate(Mockito.anyList());
	}

}
