package rcms.utilities.daqexpert.segmentation;

import rcms.utilities.daqexpert.Point;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DAQConverter {

	public static Point convertToRatePoint(DummyDAQ daq) {
		return new Point(daq.getLastUpdate(), daq.getRate());
	}

	public static Point convertToEventPoint(DummyDAQ daq) {
		return new Point(daq.getLastUpdate(), daq.getEvents());
	}

}
