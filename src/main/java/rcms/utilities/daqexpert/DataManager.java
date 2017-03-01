package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.segmentation.DAQConverter;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.segmentation.DataResolutionManager;
import rcms.utilities.daqexpert.segmentation.LinearSegmentator;
import rcms.utilities.daqexpert.segmentation.SegmentationSettings;
import rcms.utilities.daqexpert.segmentation.StreamProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DataManager {

	private static final Logger logger = Logger.getLogger(DataManager.class);

	private Date lastUpdate;

	public Map<String, Set<Condition>> experimental;

	private final DataResolutionManager dataResolutionManager;

	public DataManager() {
		experimental = new HashMap<>();
		experimental.put("test", new HashSet<Condition>());

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

	}

	public List<Point> addSnapshot(DummyDAQ dummyDAQ) {

		logger.debug("New snapshot received");

		List<Point> readyToPersist = new ArrayList<Point>();

		readyToPersist.add(DAQConverter.convertToRatePoint(dummyDAQ));
		readyToPersist.add(DAQConverter.convertToEventPoint(dummyDAQ));

		Map<DataResolution, Boolean> resultsReady = dataResolutionManager.queue(dummyDAQ);

		if (resultsReady.get(DataResolution.Minute)) {
			readyToPersist
					.addAll(transferData(DataResolution.Minute, dataResolutionManager.getMinuteStreamProcessor()));
		}
		if (resultsReady.get(DataResolution.Hour)) {
			readyToPersist.addAll(transferData(DataResolution.Hour, dataResolutionManager.getHourStreamProcessor()));
		}
		if (resultsReady.get(DataResolution.Day)) {
			readyToPersist.addAll(transferData(DataResolution.Day, dataResolutionManager.getDayStreamProcessor()));
		}
		if (resultsReady.get(DataResolution.Month)) {
			readyToPersist.addAll(transferData(DataResolution.Month, dataResolutionManager.getMonthStreamProcessor()));
		}

		return readyToPersist;
	}

	/**
	 * 
	 * @param resolution
	 * @param streamProcessor
	 */
	private List<Point> transferData(DataResolution resolution, StreamProcessor streamProcessor) {

		logger.debug("Transfering segmentated data of " + resolution + " resolution");
		List<Point> rate = streamProcessor.getOutput().get(DataStream.RATE);
		List<Point> events = streamProcessor.getOutput().get(DataStream.EVENTS);

		List<Point> readyToPersist = new ArrayList<Point>();
		for (Point curr : rate) {
			curr.setGroup(DataStream.RATE.ordinal());
			curr.setResolution(resolution.ordinal());
			readyToPersist.add(curr);
		}

		for (Point curr : events) {
			curr.setGroup(DataStream.EVENTS.ordinal());
			curr.setResolution(resolution.ordinal());
			readyToPersist.add(curr);
		}

		rate.clear();
		events.clear();

		return readyToPersist;
	}

	public DataResolutionManager getDataResolutionManager() {
		return dataResolutionManager;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
