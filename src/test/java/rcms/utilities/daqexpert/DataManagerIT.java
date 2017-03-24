package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.segmentation.DataResolution;

public class DataManagerIT {

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
		List<Point> result = new ArrayList<Point>();

		for (int i = 0; i < 100000; i++) {
			Pair<Long, Integer> a = getSquareWave();
			int value1 = a.getRight();
			int value2 = -a.getRight() / 2;
			long timestamp = a.getLeft();
			result.addAll(dm.addSnapshot(TestDummyDAQFactory.of(timestamp, value1, value2)));
		}

		int[] rates = { 100000, 3559, 341, 36, 0 };
		int[] events = { 100000, 5296, 543, 46, 0 };
		assertResults(result, rates, events);

	}

	@Test
	public void segmentationOfSinFunctionTest() {

		DataManager dm = new DataManager();

		List<Point> result = new ArrayList<Point>();
		for (int i = 0; i < 100000; i++) {
			Pair<Long, Integer> a = multipleSins();
			int value1 = a.getRight();
			int value2 = -a.getRight() / 2;
			long timestamp = a.getLeft();
			result.addAll(dm.addSnapshot(TestDummyDAQFactory.of(timestamp, value1, value2)));
		}

		int[] rates = { 100000, 7676, 792, 72, 0 };
		int[] events = { 100000, 11886, 1188, 108, 0 };
		assertResults(result, rates, events);
	}

	private void assertResults(List<Point> results, int[] rate, int[] event) {
		int rateFullResSize = 0;
		int rateMonthResSize = 0;
		int rateDayResSize = 0;
		int rateHourResSize = 0;
		int rateMinuteResSize = 0;
		int eventsFullResSize = 0;
		int eventsMonthResSize = 0;
		int eventsDayResSize = 0;
		int eventsHourResSize = 0;
		int eventsMinuteResSize = 0;
		for (Point r : results) {
			if (r.getResolution() == DataResolution.Full.ordinal() && r.getGroup() == DataStream.RATE.ordinal()) {
				rateFullResSize++;
			}
			if (r.getResolution() == DataResolution.Full.ordinal() && r.getGroup() == DataStream.EVENTS.ordinal()) {
				eventsFullResSize++;
			}

			if (r.getResolution() == DataResolution.Month.ordinal() && r.getGroup() == DataStream.RATE.ordinal()) {
				rateMonthResSize++;
			}
			if (r.getResolution() == DataResolution.Month.ordinal() && r.getGroup() == DataStream.EVENTS.ordinal()) {
				eventsMonthResSize++;
			}

			if (r.getResolution() == DataResolution.Day.ordinal() && r.getGroup() == DataStream.RATE.ordinal()) {
				rateDayResSize++;
			}
			if (r.getResolution() == DataResolution.Day.ordinal() && r.getGroup() == DataStream.EVENTS.ordinal()) {
				eventsDayResSize++;
			}

			if (r.getResolution() == DataResolution.Hour.ordinal() && r.getGroup() == DataStream.RATE.ordinal()) {
				rateHourResSize++;
			}
			if (r.getResolution() == DataResolution.Hour.ordinal() && r.getGroup() == DataStream.EVENTS.ordinal()) {
				eventsHourResSize++;
			}
			if (r.getResolution() == DataResolution.Minute.ordinal() && r.getGroup() == DataStream.RATE.ordinal()) {
				rateMinuteResSize++;
			}
			if (r.getResolution() == DataResolution.Minute.ordinal() && r.getGroup() == DataStream.EVENTS.ordinal()) {
				eventsMinuteResSize++;
			}
		}

		Assert.assertEquals(rate[0], rateFullResSize);
		Assert.assertEquals(event[0], eventsFullResSize);
		Assert.assertEquals(rate[1], rateMinuteResSize);
		Assert.assertEquals(event[1], eventsMinuteResSize);
		Assert.assertEquals(rate[2], rateHourResSize);
		Assert.assertEquals(event[2], eventsHourResSize);
		Assert.assertEquals(rate[3], rateDayResSize);
		Assert.assertEquals(event[3], eventsDayResSize);
		Assert.assertEquals(rate[4], rateMonthResSize);
		Assert.assertEquals(event[4], eventsMonthResSize);
	}

	private void printToCSV(List<Point> points) {

		// may be interesing to see the results as a chart in e.g. excel

	}

}
