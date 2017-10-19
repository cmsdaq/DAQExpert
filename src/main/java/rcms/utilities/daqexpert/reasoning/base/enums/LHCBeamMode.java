package rcms.utilities.daqexpert.reasoning.base.enums;

public enum LHCBeamMode {

	/* string constants were taken from the LHC beam conditions SQL table.
	   BEAM_DUMP_WARNING was not found in this table, the value below
	   is the expected string.
	*/

	INJECTION_PROBE_BEAM("INJECTION PROBE BEAM"),
	INJECTION_SETUP_BEAM("INJECTION SETUP BEAM"),
	INJECTION_PHYSICS_BEAM("INJECTION PHYSICS BEAM"),
	PREPARE_RAMP("PREPARE RAMP"),
	RAMP("RAMP"),
	FLAT_TOP("FLAT TOP"),
	SQUEEZE("SQUEEZE"),
	ADJUST("ADJUST"),
	STABLE_BEAMS("STABLE BEAMS"),

	ABORT("ABORT"),
	SETUP("SETUP"),
	INJECT_AND_DUMP("INJECT AND DUMP"),
	CIRCULATE_AND_DUMP("CIRCULATE AND DUMP"),
	RAMP_DOWN("RAMP DOWN"),
	RECOVER("RECOVERY"),  // note the disagreement between Java symbol and string
	CYCLING("CYCLING"),
	BEAM_DUMP("BEAM DUMP"),
	UNSTABLE_BEAMS("UNSTABLE BEAMS"),
	BEAM_DUMP_WARNING("BEAM_DUMP_WARNING"), // this mode was not found in beam modes history

	NO_BEAM("NO BEAM"),

	UNKNOWN("unknown");
	
	private final String code;

	private LHCBeamMode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	/** method to convert a string describing an LHC machine
	 *  mode into an LHCBeamMode enum.
	 *
	 * @return the LHCBeamMode value corresponding to code
	 *  or UNKNOWN if not found.
	 */
	public static LHCBeamMode getModeByCode(String code) {

		// note that some beam mode stirngs have spaces in them
		// so we can't use LHCBeamMode.valueOf(code)
		for (LHCBeamMode mode : LHCBeamMode.values()) {
			if (code.equals(mode.getCode())) {
				return mode;
			}
		}

		// no matching string found
		return LHCBeamMode.UNKNOWN;
	}
}
