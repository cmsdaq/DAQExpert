package rcms.utilities.daqexpert.reasoning.base;

public enum EventGroup {
	LHC_BEAM("lhc-beam"),
	LHC_MACHINE("lhc-machine"),
	NO_RATE("no-rate"),
	RATE_OUT_OF_RANGE("rate-oor"),
	Warning("warning"),
	NO_RATE_WHEN_EXPECTED("nrwe"),
	RUN_NUMBER("run-no"),
	RUN_ONGOING("run-on"),
	SESSION_NUMBER("session-no"),
	FLOWCHART("flowchart"),
	LEVEL_ZERO("level-zero"),
	DAQ_STATE("daq-state"),
	OTHER("other");

	private final String code;

	private EventGroup(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
