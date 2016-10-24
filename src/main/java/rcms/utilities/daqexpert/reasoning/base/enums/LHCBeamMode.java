package rcms.utilities.daqexpert.reasoning.base.enums;

public enum LHCBeamMode {

	STABLE_BEAMS("STABLE BEAMS"), UNKNOWN("unknown");
	private final String code;

	private LHCBeamMode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static LHCBeamMode getModeByCode(String code) {
		if (code.equals(STABLE_BEAMS.getCode())) {
			return LHCBeamMode.STABLE_BEAMS;
		} else
			return LHCBeamMode.UNKNOWN;
	}
}
