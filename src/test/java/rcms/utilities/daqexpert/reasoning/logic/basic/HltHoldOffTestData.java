package rcms.utilities.daqexpert.reasoning.logic.basic;

/** contains information about a point in time for the testing the HLT holdoff mechanisms
 *  (CPU load, HLT bandwidth) */
public class HltHoldOffTestData {
	private final long timestamp;

	private final boolean runOngoing;

	private final boolean expectedResult;

	private float cpuLoad;

	/** HLT output bandwidth */
	private float outputBandwidth;

	public HltHoldOffTestData(long timestamp, boolean runOngoing, boolean expectedResult) {
		this.timestamp = timestamp;
		this.runOngoing = runOngoing;
		this.expectedResult = expectedResult;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isRunOngoing() {
		return runOngoing;
	}

	public boolean isExpectedResult() {
		return expectedResult;
	}

	public float getCpuLoad() {
		return cpuLoad;
	}

	/** @return this so that we can call more than one set function
	 *  or do the set right after the new and store the object somewhere.
	 */
	public HltHoldOffTestData setCpuLoad(float cpuLoad) {
		this.cpuLoad = cpuLoad;
		return this;
	}

	public float getOutputBandwidth() {
		return outputBandwidth;
	}

	public HltHoldOffTestData setOutputBandwidth(float outputBandwidth) {
		this.outputBandwidth = outputBandwidth;
		return this;
	}
}
