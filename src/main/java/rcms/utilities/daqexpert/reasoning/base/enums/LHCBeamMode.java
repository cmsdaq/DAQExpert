package rcms.utilities.daqexpert.reasoning.base.enums;

public enum LHCBeamMode {

	INJECTION_PROBE_BEAM(""),
	INJECTION_SETUP_BEAM(""),
	INJECTION_PHYSICS_BEAM(""),
	PREPARE_RAMP(""),
	RAMP(""),
	FLAT_TOP(""),
	SQUEEZE(""),
	ADJUST(""),
	STABLE_BEAMS("STABLE BEAMS"),

	ABORT(""),
	SETUP(""),
	INJECT_AND_DUMP(""),
	CIRCULATE_AND_DUMP(""),
	RAMP_DOWN(""),
	RECOVER(""),
	CYCLING(""),
	BEAM_DUMP(""),
	UNSTABLE_BEAMS(""),
	BEAM_DUMP_WARNING(""),
	UNKNOWN("unknown");
	
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
