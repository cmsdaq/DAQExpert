package rcms.utilities.daqexpert.reasoning.base;

public enum EventGroup {
	LHC_BEAM("lhc-beam"),
	LHC_MACHINE("lhc-machine"),
	NO_RATE("no-rate"),
	RATE_OUT_OF_RANGE("rate-oor"),
	Warning("warning"),
	Error("error"),
	RUN_NUMBER("run-no"),
	RUN_ONGOING("run-on"),
	SESSION_NUMBER("session-no"),
	FLOWCHART("flowchart"),
	LEVEL_ZERO("level-zero"),
	DAQ_STATE("daq-state"),
	OTHER("other"),
	FL1("fl1"),
	FL2("fl2"),
	FL3("fl3"),
	FL4("fl4"),
	FL5("fl5"),
	FL6("fl6");

	private final String code;

	private EventGroup(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
