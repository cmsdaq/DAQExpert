package rcms.utilities.daqaggregator.servlets;

/**
 * Enumeration of data resolution
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum DataResolution {

	Full(60 * 60 * 1000), // show full resoltion when range less than 60 min.
	Minute(48* 60 * 60 * 1000), // show minute resolution when range less than 48h
	Hour(5* 24 * 60 * 60 * 1000), // show hour range when range less than 5 d
	Day(6* 30 * 24 * 60 * 60 * 1000), // show day range when range less than 6m
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
