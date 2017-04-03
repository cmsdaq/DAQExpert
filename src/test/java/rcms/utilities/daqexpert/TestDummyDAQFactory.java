package rcms.utilities.daqexpert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class TestDummyDAQFactory {
	public static DummyDAQ of(long timestamp, int value1, int value2) {
		DummyDAQ daq = new DummyDAQ();
		daq.setLastUpdate(timestamp);
		daq.setEvents(value1);
		daq.setRate(value2);
		return daq;
	}

	private static void print(List<Point> list, String name) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		System.out.println(
				"var " + name + " = " + om.writeValueAsString(list.subList(0, Math.min(list.size(), 400))) + ";");
	}
}
