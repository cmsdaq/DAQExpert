package rcms.utilities.daqexpert.segmentation;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.Point;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DataResolutionManager {

	private static final Logger logger = Logger.getLogger(DataResolutionManager.class);

	private final StreamProcessor minuteStreamProcessor;
	private final StreamProcessor hourStreamProcessor;
	private final StreamProcessor dayStreamProcessor;
	private final StreamProcessor monthStreamProcessor;

	public DataResolutionManager() {
		this.minuteStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Minute), 100);
		this.hourStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Hour), 1000);
		this.dayStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Day), 10000);
		this.monthStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Month), 100000);
		// this.dataManager = dataManager;

	}

	private boolean queueInProcessor(StreamProcessor current, DummyDAQ daq) {
		current.getInput().get(DataStream.RATE).add(DAQConverter.convertToRatePoint(daq));
		current.getInput().get(DataStream.EVENTS).add(DAQConverter.convertToEventPoint(daq));

		if (current.getInput().get(DataStream.RATE).size() > current.getThreshold()) {
			logger.debug("Data of " + current.getClass().getName() + " will be segmented");
			current.segmentateInput();
			return true;
		}
		return false;
	}

	public Map<Resolution, Boolean> queue(DummyDAQ daq) {

		Map<Resolution, Boolean> result = new HashMap<>();

		result.put(Resolution.Minute, queueInProcessor(minuteStreamProcessor, daq));
		result.put(Resolution.Hour, queueInProcessor(hourStreamProcessor, daq));
		result.put(Resolution.Day, queueInProcessor(dayStreamProcessor, daq));
		result.put(Resolution.Month, queueInProcessor(monthStreamProcessor, daq));
		return result;

	}

	public StreamProcessor getMinuteStreamProcessor() {
		return minuteStreamProcessor;
	}

	public StreamProcessor getHourStreamProcessor() {
		return hourStreamProcessor;
	}

	public StreamProcessor getDayStreamProcessor() {
		return dayStreamProcessor;
	}

	public StreamProcessor getMonthStreamProcessor() {
		return monthStreamProcessor;
	}

}
