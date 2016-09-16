package rcms.utilities.daqexpert.segmentation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DataResolutionManager {

	long timestampOfLastMinute = 0L;
	long timestampOfLastHour = 0L;
	long timestampOfLastDay = 0L;
	long timestampOfLastMonth = 0L;

	private static final Logger logger = Logger.getLogger(DataResolutionManager.class);

	/**
	 * Convert data to given resolution
	 * 
	 * @param processAfterTimestamp
	 *            convert streams only after given timestamp (for all pass 0)
	 * @param targetResolution
	 *            target resolution of stream use values from {@link Calendar}
	 * @see {@link Calendar#DAY_OF_MONTH}
	 * @see {@link Calendar#HOUR_OF_DAY}
	 * @see {@link Calendar#MINUTE}
	 * 
	 * @param targetStream
	 *            target stream
	 * @return
	 */
	private long prepareData(long processAfterTimestamp, long processBeforeTimestamp, int targetResolution,
			List<DummyDAQ> targetStream) {
		int last = -1;
		int elements = 0;
		int thisround = 0;
		DummyDAQ processedDaq = new DummyDAQ();
		Calendar calendar = Calendar.getInstance();

		List<DummyDAQ> list = DataManager.get().rawData;
		synchronized (list) {

			for (DummyDAQ daq : list) {

				long curr = daq.getLastUpdate();

				if (curr > processAfterTimestamp && curr < processBeforeTimestamp) {
					elements++;

					Date date = new Date(daq.getLastUpdate());

					calendar.setTime(date);

					// current value of minute/hour/day depending on field
					int currentValue = calendar.get(targetResolution);

					if (last == -1)
						last = currentValue;

					processedDaq.setRate(processedDaq.getRate() + daq.getRate());
					processedDaq.setEvents(processedDaq.getEvents() + daq.getEvents());

					/* finish current daq */
					if (last != currentValue) {

						logger.debug("Next value " + last + "!=" + currentValue);

						processedDaq.setRate(processedDaq.getRate() / elements);
						processedDaq.setEvents(processedDaq.getEvents() / elements);

						Calendar cal = Calendar.getInstance();
						cal.setTime(date);

						if (targetResolution == Calendar.DAY_OF_MONTH) {
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
						} else if (targetResolution == Calendar.HOUR_OF_DAY) {
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
						} else if (targetResolution == Calendar.MINUTE) {
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
						}

						Date refinedDate = cal.getTime();

						processedDaq.setLastUpdate(refinedDate.getTime());
						processAfterTimestamp = refinedDate.getTime();
						thisround++;
						elements = 0;
						last = currentValue;
						targetStream.add(processedDaq);
						processedDaq = new DummyDAQ();
					}
					/* process another value to daq */
					else {
						logger.debug("Same minute already for " + elements + " time");
					}
				}
			}
		}

		logger.trace("Prepared minute resolution data (" + DataManager.get().rawDataMinute.size()
				+ " entries) from raw data (" + DataManager.get().rawData.size() + " entries), " + thisround
				+ " in this round");
		logger.debug("Prepared minute data" + thisround + " in this round");
		return processAfterTimestamp;

	}

	public void prepareMultipleResolutionDataForPast(long before) {
		List<DummyDAQ> minuteTargetStream = new ArrayList<>();
		timestampOfLastMinute = prepareData(timestampOfLastMinute, before, Calendar.MINUTE, minuteTargetStream);
		List<DummyDAQ> rawDataMinutes = DataManager.get().rawDataMinute;
		synchronized (rawDataMinutes) {
			rawDataMinutes.addAll(0, minuteTargetStream);
		}

		List<DummyDAQ> hourTargetStream = new ArrayList<>();
		timestampOfLastHour = prepareData(timestampOfLastHour, before, Calendar.HOUR_OF_DAY, hourTargetStream);
		List<DummyDAQ> rawDataHours = DataManager.get().rawDataHour;
		synchronized (rawDataHours) {
			rawDataHours.addAll(0, hourTargetStream);
		}

		List<DummyDAQ> dayTargetStream = new ArrayList<>();
		timestampOfLastHour = prepareData(timestampOfLastHour, before, Calendar.DAY_OF_MONTH, dayTargetStream);
		List<DummyDAQ> rawDataDays = DataManager.get().rawDataDay;
		synchronized (rawDataDays) {
			rawDataDays.addAll(0, dayTargetStream);
		}

		List<DummyDAQ> monthTargetStream = new ArrayList<>();
		timestampOfLastHour = prepareData(timestampOfLastHour, before, Calendar.MONTH, monthTargetStream);
		List<DummyDAQ> rawDataMonths = DataManager.get().rawDataMonth;
		synchronized (rawDataMonths) {
			rawDataMonths.addAll(0, monthTargetStream);
		}
	}

	public void prepareMultipleResolutionData() {
		timestampOfLastMinute = prepareData(timestampOfLastMinute, Long.MAX_VALUE, Calendar.MINUTE,
				DataManager.get().rawDataMinute);
		timestampOfLastHour = prepareData(timestampOfLastHour, Long.MAX_VALUE, Calendar.HOUR_OF_DAY,
				DataManager.get().rawDataHour);
		timestampOfLastDay = prepareData(timestampOfLastDay, Long.MAX_VALUE, Calendar.DAY_OF_MONTH,
				DataManager.get().rawDataDay);
		timestampOfLastMonth = prepareData(timestampOfLastMonth, Long.MAX_VALUE, Calendar.MONTH,
				DataManager.get().rawDataMonth);
	}

}
