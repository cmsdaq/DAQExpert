package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.Entry;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
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
	// TODO: is it optimal? move key to one place
	// TODO: make it a singleton
	private static final PersistenceManager persistenceManager = new PersistenceManager("history");

	/** All produced reasons are kept in this list */
	@Deprecated
	private Set<Entry> result;

	private Date lastUpdate;

	/**
	 * TODO: check if this field is really necessary
	 */
	public CircularFifoQueue<DAQ> buf;

	public Map<String, Set<Entry>> experimental;

	private final DataResolutionManager dataResolutionManager;

	public DataManager() {
		buf = new CircularFifoQueue<>(5000);
		experimental = new HashMap<>();
		experimental.put("test", new HashSet<Entry>());

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

	public void addSnapshot(DummyDAQ dummyDAQ) {

		logger.debug("New snapshot received");

		List<Point> readyToPersist = new ArrayList<Point>();

		readyToPersist.add(DAQConverter.convertToRatePoint(dummyDAQ));
		readyToPersist.add(DAQConverter.convertToEventPoint(dummyDAQ));
		persistenceManager.persist(readyToPersist);

		Map<DataResolution, Boolean> resultsReady = dataResolutionManager.queue(dummyDAQ);

		if (resultsReady.get(DataResolution.Minute)) {
			transferData(DataResolution.Minute, dataResolutionManager.getMinuteStreamProcessor());
		}
		if (resultsReady.get(DataResolution.Hour)) {
			transferData(DataResolution.Hour, dataResolutionManager.getHourStreamProcessor());
		}
		if (resultsReady.get(DataResolution.Day)) {
			transferData(DataResolution.Day, dataResolutionManager.getDayStreamProcessor());
		}
		if (resultsReady.get(DataResolution.Month)) {
			transferData(DataResolution.Month, dataResolutionManager.getMonthStreamProcessor());
		}
	}

	/**
	 * 
	 * @param resolution
	 * @param streamProcessor
	 */
	private void transferData(DataResolution resolution, StreamProcessor streamProcessor) {

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

		persistenceManager.persist(readyToPersist);

		rate.clear();
		events.clear();
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


	/**
	 * Get all results produced by event producer
	 * 
	 * @return list of events produced
	 */
	@Deprecated
	private Set<Entry> getResult() {
		return result;
	}


}
