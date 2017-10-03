package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

/**
 * Class which can be used to apply a certain holdoff time to a condition,
 * i.e. to return true only when the input has been true for a given amount
 * of time.
 */
public class HoldOffTimer
{
	/** holdoff period in milliseconds */
	private final long period;

	/** the time when the input was set to true or -1 if it is currently false.
	 *  From this, the duration of 'uninterrupted true' can be calculated.
	 */
	private long trueStartTime = -1;

	private boolean input = false;

	/** @param period period in milliseconds for which the output should
	 *  still be false even if the input has been continuously true.
	 */
	public HoldOffTimer(long period) {
		this.period = period;
	}

	/** @param now is the current time (in milliseconds since epoch), typically
	 *  taken from the DAQ snapshot in question.
	 */
	public void updateInput(boolean input, long now) {

		this.input = input;

		if (input) {
			// start the timer if not yet started
			if (trueStartTime == -1) {
				trueStartTime = now;
			}
		} else {
			// input is still false or went back to false -- reset the timer
			trueStartTime = -1;
		}

	}

	/** @return true if the last input state has been true
	 *  since 'period' milliseconds or longer, false otherwise
	 *
	 *  @param now is the current time (in milliseconds since epoch), typically
	 *  taken from the DAQ snapshot in question.
	 */
	public boolean getOutput(long now) {

		if (! input) {
			return false;
		}

		// last input state was true, check for how long this has been
		// the case
		long duration = now - trueStartTime;

		if (duration >= period) {
			// hold off period expired
			return true;

		} else {
			// we are still in the holdoff period
		  return false;
		}
	}

	/** @return the input state (mostly used for testing / debugging) */
	public boolean getInput()
	{
		return this.input;
	}
}