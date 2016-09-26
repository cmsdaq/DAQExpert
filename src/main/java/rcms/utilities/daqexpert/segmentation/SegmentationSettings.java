package rcms.utilities.daqexpert.segmentation;

/**
 * Settings for segmentating data in different scales. There should be O(100)
 * points passed to presentation layer independently on scale (few minutes or
 * few months @see {@link DataResolution} for available scales)
 * 
 * There is one snapshot each ~2 seconds = 30/min, 1800/h 43k/day
 * 
 * @see {@link DataResolution}
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum SegmentationSettings {

	/**
	 * @see DataResolution#Minute
	 */
	Minute(2, 10),

	/**
	 * @see DataResolution#Hour
	 */
	Hour(2, 100),

	/**
	 * @see DataResolution#Day
	 */
	Day(2, 1000),

	/**
	 * @see DataResolution#Month
	 */
	Month(2, 10000);

	private SegmentationSettings(double TI, double creTh) {
		this.TI = TI;
		this.creTh = creTh;
	}

	/**
	 * TI - Turning Influence. (>=1)
	 */
	private final double TI;

	/**
	 * CRETH - Cumulative Radian Error Threshold (>0)
	 */
	private final double creTh;

	public double getTI() {
		return TI;
	}

	public double getCreTh() {
		return creTh;
	}

}
