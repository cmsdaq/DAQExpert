package rcms.utilities.daqexpert.segmentation;

import java.util.Date;

import javax.print.attribute.ResolutionSyntax;

import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.processing.DataStream;
import rcms.utilities.daqexpert.reasoning.logic.basic.RateOutOfRange;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class DAQConverter {

	public static Point convertToRatePoint(DummyDAQ daq) {
		Point point = new Point();
		point.setResolution(DataResolution.Full.ordinal());
		point.setGroup(DataStream.RATE.ordinal());
		point.setX(new Date(daq.getLastUpdate()));
		point.setY(daq.getRate());
		return point;
	}

	public static Point convertToEventPoint(DummyDAQ daq) {
		Point point = new Point();
		point.setResolution(DataResolution.Full.ordinal());
		point.setGroup(DataStream.EVENTS.ordinal());
		point.setX(new Date(daq.getLastUpdate()));
		point.setY(daq.getEvents());
		return point;
	}

}
