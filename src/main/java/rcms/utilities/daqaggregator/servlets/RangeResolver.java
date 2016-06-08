package rcms.utilities.daqaggregator.servlets;

import java.util.Date;

/**
 * Resolves data resolution based on given start and end date.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class RangeResolver {

	public DataResolution resolve(Date start, Date end) {

		long diffInMiliseconds = end.getTime() - start.getTime();

		if (diffInMiliseconds < DataResolution.Full.getMaxDifference()) {
			return DataResolution.Full;
		} else if (diffInMiliseconds < DataResolution.Minute.getMaxDifference()) {
			return DataResolution.Minute;
		} else if (diffInMiliseconds < DataResolution.Hour.getMaxDifference()) {
			return DataResolution.Hour;
		} else if (diffInMiliseconds < DataResolution.Day.getMaxDifference()) {
			return DataResolution.Day;
		} else {
			return DataResolution.Month;
		}

	}
}
