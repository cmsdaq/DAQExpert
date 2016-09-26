package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.processing.SortedArrayList;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.segmentation.DAQConverter;
import rcms.utilities.daqexpert.segmentation.DataResolutionManager;
import rcms.utilities.daqexpert.segmentation.Resolution;
import rcms.utilities.daqexpert.segmentation.StreamProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DataManager {

	private DataResolutionManager dataResolutionManager;

	public DataManager() {
		buf = new CircularFifoQueue<>(5000);
		rawData = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());

		rawDataByResolution = new HashMap<>();

		rawDataMinute = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataHour = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataDay = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataMonth = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		result = Collections.synchronizedList(new ArrayList<Entry>());

		this.dataResolutionManager = new DataResolutionManager();

		initialize();
	}

	/** All produced reasons are kept in this list */
	private final List<Entry> result;

	public CircularFifoQueue<DAQ> buf;

	public void addSnapshot(DummyDAQ dummyDAQ) {

		Map<Resolution, Boolean> a = dataResolutionManager.queue(dummyDAQ);
		rawDataByResolution.get(Resolution.Raw).get(DataStream.RATE).add(DAQConverter.convertToRatePoint(dummyDAQ));
		rawDataByResolution.get(Resolution.Raw).get(DataStream.EVENTS).add(DAQConverter.convertToEventPoint(dummyDAQ));

		if (a.get(Resolution.Minute)) {
			transferData(Resolution.Minute, dataResolutionManager.getMinuteStreamProcessor());
		}
		if (a.get(Resolution.Hour)) {
			transferData(Resolution.Hour, dataResolutionManager.getHourStreamProcessor());
		}
		if (a.get(Resolution.Day)) {
			transferData(Resolution.Day, dataResolutionManager.getDayStreamProcessor());
		}
		if (a.get(Resolution.Month)) {
			transferData(Resolution.Month, dataResolutionManager.getMonthStreamProcessor());
		}
	}

	private void initialize() {
		rawDataByResolution.put(Resolution.Raw, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(Resolution.Minute, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(Resolution.Hour, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(Resolution.Day, new HashMap<DataStream, List<Point>>());
		rawDataByResolution.put(Resolution.Month, new HashMap<DataStream, List<Point>>());

		rawDataByResolution.get(Resolution.Raw).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Raw).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Minute).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Minute).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Hour).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Hour).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Day).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Day).put(DataStream.EVENTS, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Month).put(DataStream.RATE, new ArrayList<Point>());
		rawDataByResolution.get(Resolution.Month).put(DataStream.EVENTS, new ArrayList<Point>());
	}

	private void transferData(Resolution resolution, StreamProcessor streamProcessor) {
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
	private final Map<Resolution, Map<DataStream, List<Point>>> rawDataByResolution;

	@Deprecated
	public List<DummyDAQ> rawData;

	@Deprecated
	public List<DummyDAQ> rawDataMinute;

	@Deprecated
	public List<DummyDAQ> rawDataHour;

	@Deprecated
	public List<DummyDAQ> rawDataDay;

	@Deprecated
	public List<DummyDAQ> rawDataMonth;

	/**
	 * Get all results produced by event producer
	 * 
	 * @return list of events produced
	 */
	public List<Entry> getResult() {
		return result;
	}

	public Map<Resolution, Map<DataStream, List<Point>>> getRawDataByResolution() {
		return rawDataByResolution;
	}

}
