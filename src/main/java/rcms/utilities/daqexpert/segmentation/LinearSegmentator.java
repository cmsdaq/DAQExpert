package rcms.utilities.daqexpert.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Point;

/**
 * Linear segmentator
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class LinearSegmentator {

	private static final Logger logger = Logger.getLogger(LinearSegmentator.class);
	private final double TI;
	private final double cre_th;
	private final SegmentationSettings settings;

	private List<Point> stream;

	public LinearSegmentator(SegmentationSettings settings) {
		this.settings = settings;
		this.TI = settings.getTI();
		this.cre_th = settings.getCreTh();
	}

	private double calcRad(int i) {
		i = i - 1;
		long t1;
		long t2;
		t1 = stream.get(i + 1).x;
		t2 = stream.get(i).x;
		double dt = Math.abs(t1 - t2);

		double result = Math.atan((stream.get(i + 1).y - stream.get(i).y) / dt);
		return result;
	}

	private void prepareStream() {
		if (stream.size() == 0) {
			throw new RuntimeException("EMPTY_STREAM_SEGMENTATED");
		}
		List<Point> s = new ArrayList<Point>();
		List<Long> history = new ArrayList<Long>();
		for (int i = 0; i < stream.size(); i++) {
			if (!history.contains(stream.get(i).x)) {
				s.add(stream.get(i));
				history.add(new Long(stream.get(i).x));
			} else {
				// System.out.println("Found two values at time "
				// + stream.get(i).x);
			}

		}
		this.stream = s;
	}

	public List<Point> segmentate(List<Point> stream) {
		logger.debug("Segmenting " + stream);
		this.stream = stream;
		prepareStream();
		if (stream == null)
			return null;
		if (stream.size() < 3) {
			return stream;
		}
		List<Point> s = new ArrayList<Point>();

		int last_tp_idx;
		double[] rad_err = new double[stream.size() + 1];
		double[] rad = new double[stream.size() + 1];
		double[] cre = new double[stream.size() + 1];

		last_tp_idx = 0;
		rad_err[stream.size()] = 0;
		rad[stream.size()] = 0;

		rad_err[1] = calcRad(1);
		rad[1] = rad_err[1];

		s.add(stream.get(0));

		for (int i = 2; i < stream.size() - 1; i++) {
			rad[i] = calcRad(i);
			rad_err[i] = Math.abs(rad[i] - rad[i - 1]);

			double sum = 0d;
			for (int j = last_tp_idx + 1; j <= i; j++) {
				sum += Math.abs(rad[j] * TI);
			}

			cre[i] = rad_err[i] + sum;

			if (cre[i] >= cre_th) {
				s.add(stream.get(i - 1));
				last_tp_idx = i;
			} else {
				// System.out.println("Ommit value at " + stream.get(i - 1).x
				// + ", " + cre[i]);
			}
		}

		s.add(stream.get(stream.size() - 1));

		return s;
		// return this.stream;
	}

	public SegmentationSettings getSettings() {
		return settings;
	}
}