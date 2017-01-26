package rcms.utilities.daqexpert;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static void print(DataManager dm) {
		System.out.println("RAW:" + dm.getRawDataByResolution().get(DataResolution.Full).get(DataStream.RATE).size());
		System.out.println("MIN:" + dm.getRawDataByResolution().get(DataResolution.Minute).get(DataStream.RATE).size());
		System.out.println("HOR:" + dm.getRawDataByResolution().get(DataResolution.Hour).get(DataStream.RATE).size());
		System.out.println("DAY:" + dm.getRawDataByResolution().get(DataResolution.Day).get(DataStream.RATE).size());
		System.out.println("MON:" + dm.getRawDataByResolution().get(DataResolution.Month).get(DataStream.RATE).size());

		try {
			print(dm.getRawDataByResolution().get(DataResolution.Full).get(DataStream.RATE), "raw");
			print(dm.getRawDataByResolution().get(DataResolution.Minute).get(DataStream.RATE), "minute");
			print(dm.getRawDataByResolution().get(DataResolution.Hour).get(DataStream.RATE), "hour");
			print(dm.getRawDataByResolution().get(DataResolution.Day).get(DataStream.RATE), "day");
			print(dm.getRawDataByResolution().get(DataResolution.Month).get(DataStream.RATE), "month");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private static void print(List<Point> list, String name) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		System.out.println(
				"var " + name + " = " + om.writeValueAsString(list.subList(0, Math.min(list.size(), 400))) + ";");
	}
}
