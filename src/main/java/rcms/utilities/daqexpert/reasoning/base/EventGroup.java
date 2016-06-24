package rcms.utilities.daqexpert.reasoning.base;

public enum EventGroup {
	LHC("lhc"),
	Info("info"),
	Warning("warning"),
	Error("error"),
	Run("run"),
	FLOWCHART("flowchart"),
	DAQ("daq"),
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
