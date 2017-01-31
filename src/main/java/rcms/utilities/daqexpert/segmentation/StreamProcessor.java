package rcms.utilities.daqexpert.segmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.processing.DataStream;

public class StreamProcessor {

	/**
	 * Processing threshold
	 */
	private final int threshold;

	private int count;

	private long lastTimestamp;

	private final LinearSegmentator linearSegmentator;

	private final HashMap<DataStream, List<Point>> input;
	private final HashMap<DataStream, List<Point>> output;

	public StreamProcessor(LinearSegmentator linearSegmentator, SegmentationSettings settings) {
		this.lastTimestamp = 0L;
		this.count = 0;
		this.linearSegmentator = linearSegmentator;
		this.threshold = settings.getThreshold();

		this.input = new HashMap<>();
		this.output = new HashMap<>();
		this.input.put(DataStream.RATE, new ArrayList<Point>());
		this.input.put(DataStream.EVENTS, new ArrayList<Point>());
		this.output.put(DataStream.RATE, new ArrayList<Point>());
		this.output.put(DataStream.EVENTS, new ArrayList<Point>());
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public LinearSegmentator getLinearSegmentator() {
		return linearSegmentator;
	}

	public int getThreshold() {
		return threshold;
	}

	public HashMap<DataStream, List<Point>> getInput() {
		return input;
	}

	public HashMap<DataStream, List<Point>> getOutput() {
		return output;
	}

	public void segmentateInput() {
		List<Point> a = linearSegmentator.segmentate(input.get(DataStream.RATE));
		List<Point> b = linearSegmentator.segmentate(input.get(DataStream.EVENTS));
		input.get(DataStream.RATE).clear();
		input.get(DataStream.EVENTS).clear();
		output.get(DataStream.RATE).addAll(a);
		output.get(DataStream.EVENTS).addAll(b);

	}

}
