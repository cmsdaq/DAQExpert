package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class TaskManager {

	private static TaskManager instance;

	private static final Logger logger = Logger.getLogger(TaskManager.class);

	private TaskManager() {
		buf = new CircularFifoQueue<>(5000);
		rawData = new ArrayList<>();
		rawDataMinute = new ArrayList<>();
		rawDataHour = new ArrayList<>();
		rawDataDay = new ArrayList<>();
		rawDataMonth = new ArrayList<>();
	}

	public static TaskManager get() {
		if (instance == null)
			instance = new TaskManager();
		return instance;
	}

	public CircularFifoQueue<DAQ> buf;

	public List<DummyDAQ> rawData;
	public List<DummyDAQ> rawDataMinute;
	public List<DummyDAQ> rawDataHour;
	public List<DummyDAQ> rawDataDay;
	public List<DummyDAQ> rawDataMonth;

	public static void main(String[] args) {
		TaskManager tm = TaskManager.get();
	}

}
