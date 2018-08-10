package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import rcms.utilities.daqexpert.reasoning.logic.basic.helper.HoldOffTimer;

/** class with common code for holdoff logic for problems which are
 *  ignored at the beginning of a run.
 *  Originally factored out from class HltCpuLoad
 */
public class BeginningOfRunHoldOffLogic {

	/** threshold above which the value of the 'metric' (e.g. cpu load)
	 *  is considered bad.
	 */
	private final float maxMetricValue;

	/**
	 *  Timer keeping condition of for a period after RunOngoing condition satisfied
	 */
	private final HoldOffTimer runOngoingHoldOffTimer;

	/**
	 * Timer which keeps condition off for a period after self condition satisfied
	 */
	private final HoldOffTimer thresholdHoldOffTimer;

	/** whether a run is ongoing or not */
	private boolean runOngoing;

	/** timestamp of current DAQ state update */
	private Long now;

	/** indicates that the metric is above threshold */
	private boolean thresholdExceeded;

	/**
	 * @param maxMetricValue is the threshold for the metric to be monitored
	 *                        (e.g. HLT CPU load)
	 * @param runOngoingHoldOffPeriod is the holdoff period in milliseconds
	 * @param thresholdHoldOffPeriod is the holdoff period in milliseconds
	 *                               during which the condition (metric to be
	 *                               monitored above threshold) must be true
	 *                               for the logic module to fire
	 */
	public BeginningOfRunHoldOffLogic(float maxMetricValue, long runOngoingHoldOffPeriod, long thresholdHoldOffPeriod) {
		this.maxMetricValue = maxMetricValue;
		runOngoingHoldOffTimer = new HoldOffTimer(runOngoingHoldOffPeriod);
		thresholdHoldOffTimer = new HoldOffTimer(thresholdHoldOffPeriod);
	}

	/** Update internal state with new results. Must be called
	 *  before any other function returning a boolean is called.
	 *
	 *  @param metricValue is the value to be compared to a threshold
	 *                     (this can also be null)
	 */
	public void updateInput(boolean runOngoing, Long now, Float metricValue) {

		this.runOngoing = runOngoing;
		this.now = now;

		// update the holdoff timer for the beginning of the run
		runOngoingHoldOffTimer.updateInput(runOngoing, now);

		thresholdExceeded = metricValue != null && metricValue > maxMetricValue;

		// update the holdoff timer for the minimum time the threshold
		// must be exceeded
		thresholdHoldOffTimer.updateInput(thresholdExceeded, now);
	}

	/** @return true if the metric is above the allowed threshold.
	 *  Call updateInput() before calling this method. */
	public boolean isThresholdExceeded() {
		return thresholdExceeded;
	}

	/** @return true iff the error condition is satisfied, i.e. the metric is above
	 *  threshold and the holdoff logic does not protect the error condition anymore.
	 */
	public boolean satisfied() {

		boolean ignoreRunOngoingHoldoff = ! runOngoing;

		// check all the conditions now
		return (thresholdExceeded &&
						( ignoreRunOngoingHoldoff || (runOngoing && runOngoingHoldOffTimer.getOutput(now))) &&
						thresholdHoldOffTimer.getOutput(now));
	}
}
