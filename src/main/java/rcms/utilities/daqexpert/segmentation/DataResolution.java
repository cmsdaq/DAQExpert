package rcms.utilities.daqexpert.segmentation;

/**
 * Enumeration of data resolution
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum DataResolution {

	Full(60 * 60 * 1000L), // full resoltion when range less than 60 min.
	Minute(48 * 60 * 60 * 1000L), // minute resolution when range less than 48h
	Hour(60 * 24 * 60 * 60 * 1000L), // hour range when range less than 60 d
	Day(48 * 30 * 24 * 60 * 60 * 1000L), // day range when range less than 48m
	Month(Long.MAX_VALUE);

	/**
	 * Max difference in milliseconds
	 */
	private final long maxDifference;

	private DataResolution(long maxDifference) {
		this.maxDifference = maxDifference;
	}

	public long getMaxDifference() {
		return maxDifference;
	}

}
