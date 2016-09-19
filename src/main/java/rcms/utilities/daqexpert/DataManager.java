package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.SortedArrayList;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DataManager {

	private static DataManager instance;

	public static DataManager get() {
		if (instance == null)
			instance = new DataManager();
		return instance;
	}

	private DataManager() {
		buf = new CircularFifoQueue<>(5000);
		rawData = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataMinute = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataHour = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataDay = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		rawDataMonth = Collections.synchronizedList(new SortedArrayList<DummyDAQ>());
		result = Collections.synchronizedList(new ArrayList<Entry>());
	}

	/** All produced reasons are kept in this list */
	private final List<Entry> result;

	public CircularFifoQueue<DAQ> buf;

	public List<DummyDAQ> rawData;
	public List<DummyDAQ> rawDataMinute;
	public List<DummyDAQ> rawDataHour;
	public List<DummyDAQ> rawDataDay;
	public List<DummyDAQ> rawDataMonth;

	/**
	 * Get all results produced by event producer
	 * 
	 * @return list of events produced
	 */
	public List<Entry> getResult() {
		return result;
	}

}
