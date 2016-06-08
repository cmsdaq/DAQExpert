package rcms.utilities.daqaggregator.reasoning.base;

public enum Level {
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
	FL4("fl4");

	private final String code;

	private Level(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
