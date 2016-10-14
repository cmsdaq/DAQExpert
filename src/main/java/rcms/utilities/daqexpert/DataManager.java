package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.segmentation.DAQConverter;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.segmentation.DataResolutionManager;
import rcms.utilities.daqexpert.segmentation.LinearSegmentator;
import rcms.utilities.daqexpert.segmentation.Point;
import rcms.utilities.daqexpert.segmentation.SegmentationSettings;
import rcms.utilities.daqexpert.segmentation.StreamProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DataManager {

	private static final Logger logger = Logger.getLogger(DataManager.class);

	private final DataResolutionManager dataResolutionManager;

	public DataManager() {
		buf = new CircularFifoQueue<>(5000);
		experimentalLMBuffer = new CircularFifoQueue<>(10);

		rawDataByResolution = new HashMap<>();

		result = Collections.synchronizedSet(new LinkedHashSet<Entry>());

		StreamProcessor minuteStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Minute),
				SegmentationSettings.Minute);
		StreamProcessor hourStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Hour),
				SegmentationSettings.Hour);
		StreamProcessor dayStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Day),
				SegmentationSettings.Day);
		StreamProcessor monthStreamProcessor = new StreamProcessor(new LinearSegmentator(SegmentationSettings.Month),
				SegmentationSettings.Month);

		this.dataResolutionManager = new DataResolutionManager(minuteStreamProcessor, hourStreamProcessor,
				dayStreamProcessor, monthStreamProcessor);

		initialize();
	}

	/** All produced reasons are kept in this list */
	private final Set<Entry> result;

	/**
	 * TODO: check if this field is really necessary
	 */
	public CircularFifoQueue<DAQ> buf;

	public CircularFifoQueue<Map<String, Entry>> experimentalLMBuffer;

	public void addSnapshot(DummyDAQ dummyDAQ) {

		logger.debug("New snapshot received");

		Map<DataResolution, Boolean> a = dataResolutionManager.queue(dummyDAQ);
		rawDataByResolution.get(DataResolution.Full).get(DataStream.RATE)
				.add(DAQConverter.convertToRatePoint(dummyDAQ));
		rawDataByResolution.get(DataResolution.Full).get(DataStream.EVENTS)
				.add(DAQConverter.convertToEventPoint(dummyDAQ));

		if (a.get(DataResolution.Minute)) {
			transferData(DataResolution.Minute, dataResolutionManager.getMinuteStreamProcessor());
		}
		if (a.get(DataResolution.Hour)) {
			transferData(DataResolution.Hour, dataResolutionManager.getHourStreamProcessor());
		}
		if (a.get(DataResolution.Day)) {
			transferData(DataResolution.Day, dataResolutionManager.getDayStreamProcessor());
		}
		if (a.get(DataResolution.Month)) {
			transferData(DataResolution.Month, dataResolutionManager.getMonthStreamProcessor());
		}
	}

	private void initialize() {
		rawDataByResolution.put(DataResolution.Full, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(DataResolution.Minute, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(DataResolution.Hour, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(DataResolution.Day, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(DataResolution.Month, new HashMap<DataStream, List<Point>>());

		rawDataByResolution.get(DataResolution.Full).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Full).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Minute).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Minute).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Hour).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Hour).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Day).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Day).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Month).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(DataResolution.Month).put(DataStream.EVENTS, new ArrayList<Point>());
	}

	private void transferData(DataResolution resolution, StreamProcessor streamProcessor) {

		logger.debug("Transfering segmentated data of " + resolution + " resolution");
		List<Point> rate = streamProcessor.getOutput().get(DataStream.RATE);
		List<Point> events = streamProcessor.getOutput().get(DataStream.EVENTS);

		rawDataByResolution.get(resolution).get(DataStream.RATE).addAll(rate);
		rawDataByResolution.get(resolution).get(DataStream.EVENTS).addAll(events);

		rate.clear();
		events.clear();
	}

	/**
	 * Processed multiresolution data
	 */
	private final Map<DataResolution, Map<DataStream, List<Point>>> rawDataByResolution;

	/**
	 * Get all results produced by event producer
	 * 
	 * @return list of events produced
	 */
	public Set<Entry> getResult() {
		return result;
	}

	public Map<DataResolution, Map<DataStream, List<Point>>> getRawDataByResolution() {
		return rawDataByResolution;
	}

	public DataResolutionManager getDataResolutionManager() {
		return dataResolutionManager;
	}

}
