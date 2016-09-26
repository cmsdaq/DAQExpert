package rcms.utilities.daqexpert;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.segmentation.DataResolution;

public class DataManagerTest {

	private long counter;

	private Pair<Long, Integer> multipleSins() {
		counter += 1000;
		double x = counter / 10000D;
		double result = 300 * Math.sin(x / 10000) + 100 * Math.cos(x / 1000) + 50 * Math.sin(x / 100)
				+ 20 * Math.cos(x / 10) + 5 * Math.sin(x) + Math.cos(10 * x);
		return Pair.of(counter, (int) (result * 1000));
	}

	private Pair<Long, Integer> getSquareWave() {
		counter += 1000;
		double angle = counter / 10000D;
		double result = Math.sin(angle) + Math.sin(3 * angle) / 3 + Math.sin(5 * angle) / 5 + Math.sin(7 * angle) / 7;
		angle += 53;
		result += 10 * (Math.sin(angle / 9) + Math.sin(3 * angle / 9) / 3 + Math.sin(5 * angle / 9) / 5
				+ Math.sin(7 * angle / 9) / 7);
		angle += 397;
		result += 100 * (Math.sin(angle / 79) + Math.sin(3 * angle / 79) / 3 + Math.sin(5 * angle / 79) / 5
				+ Math.sin(7 * angle / 79) / 7);
		return Pair.of(counter, (int) (result * 1000));
	}

	@Test
	public void segmentationOfSquareWaveFunctionTest() {

		DataManager dm = new DataManager();

		for (int i = 0; i < 100000; i++) {
			Pair<Long, Integer> a = getSquareWave();
			int value1 = a.getRight();
			int value2 = -a.getRight() / 2;
			long timestamp = a.getLeft();
			dm.addSnapshot(TestDummyDAQFactory.of(timestamp, value1, value2));
		}

		Assert.assertEquals(100000, dm.getRawDataByResolution().get(DataResolution.Full).get(DataStream.RATE).size());
		Assert.assertEquals(3559, dm.getRawDataByResolution().get(DataResolution.Minute).get(DataStream.RATE).size());
		Assert.assertEquals(341, dm.getRawDataByResolution().get(DataResolution.Hour).get(DataStream.RATE).size());
		Assert.assertEquals(36, dm.getRawDataByResolution().get(DataResolution.Day).get(DataStream.RATE).size());
		Assert.assertEquals(0, dm.getRawDataByResolution().get(DataResolution.Month).get(DataStream.RATE).size());

		// TestDummyDAQFactory.print(dm);

	}

	@Test
	public void segmentationOfSinFunctionTest() {

		DataManager dm = new DataManager();

		for (int i = 0; i < 100000; i++) {
			Pair<Long, Integer> a = multipleSins();
			int value1 = a.getRight();
			int value2 = -a.getRight() / 2;
			long timestamp = a.getLeft();
			dm.addSnapshot(TestDummyDAQFactory.of(timestamp, value1, value2));
		}

		Assert.assertEquals(100000, dm.getRawDataByResolution().get(DataResolution.Full).get(DataStream.RATE).size());
		Assert.assertEquals(7676, dm.getRawDataByResolution().get(DataResolution.Minute).get(DataStream.RATE).size());
		Assert.assertEquals(792, dm.getRawDataByResolution().get(DataResolution.Hour).get(DataStream.RATE).size());
		Assert.assertEquals(72, dm.getRawDataByResolution().get(DataResolution.Day).get(DataStream.RATE).size());
		Assert.assertEquals(0, dm.getRawDataByResolution().get(DataResolution.Month).get(DataStream.RATE).size());

		// TestDummyDAQFactory.print(dm);

	}

}
