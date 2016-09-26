package rcms.utilities.daqexpert.segmentation;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

/**
 * This class is managing incoming data and directs it to appropriate stream
 * processors in order to segmentate them
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class DataResolutionManager {

	private static final Logger logger = Logger.getLogger(DataResolutionManager.class);

	private final StreamProcessor minuteStreamProcessor;
	private final StreamProcessor hourStreamProcessor;
	private final StreamProcessor dayStreamProcessor;
	private final StreamProcessor monthStreamProcessor;

	public DataResolutionManager(StreamProcessor minuteStreamProcessor, StreamProcessor hourStreamProcessor,
			StreamProcessor dayStreamProcessor, StreamProcessor monthStreamProcessor) {
		super();
		this.minuteStreamProcessor = minuteStreamProcessor;
		this.hourStreamProcessor = hourStreamProcessor;
		this.dayStreamProcessor = dayStreamProcessor;
		this.monthStreamProcessor = monthStreamProcessor;
	}


	private boolean queueInProcessor(StreamProcessor current, DummyDAQ daq) {
		current.getInput().get(DataStream.RATE).add(DAQConverter.convertToRatePoint(daq));
		current.getInput().get(DataStream.EVENTS).add(DAQConverter.convertToEventPoint(daq));

		if (current.getInput().get(DataStream.RATE).size() > current.getThreshold()) {
			logger.debug("Data of " + current.getInput().get(DataStream.RATE).size() + " size will be segmented");
			current.segmentateInput();
			return true;
		}
		return false;
	}

	/**
	 * Method passes new data to appropriate stream processors
	 * 
	 * @param daq
	 *            new data in form of dummy daq
	 * @return map indicating if there is new data available depending on
	 *         resolution
	 */
	public Map<DataResolution, Boolean> queue(DummyDAQ daq) {

		logger.debug("Queuing data to segmentate");

		Map<DataResolution, Boolean> result = new HashMap<>();

		result.put(DataResolution.Minute, queueInProcessor(minuteStreamProcessor, daq));
		result.put(DataResolution.Hour, queueInProcessor(hourStreamProcessor, daq));
		result.put(DataResolution.Day, queueInProcessor(dayStreamProcessor, daq));
		result.put(DataResolution.Month, queueInProcessor(monthStreamProcessor, daq));
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
