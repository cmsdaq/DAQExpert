package rcms.utilities.daqexpert.reasoning.base.enums;

public enum LHCMachineMode {

	/* string constants were taken from the LHC beam conditions SQL table.
	*/

	PROTON_PHYSICS             ("PROTON PHYSICS"),
	RECOVERY                   ("RECOVERY"),
	MACHINE_DEVELOPMENT        ("MACHINE DEVELOPMENT"),
	SECTOR_DEPENDENT           ("SECTOR DEPENDENT"),
	MACHINE_CHECKOUT           ("MACHINE CHECKOUT"),
	ION_PHYSICS                ("ION PHYSICS"),
	MACHINE_TEST               ("MACHINE TEST"),
	PROTON_NUCLEUS_PHYSICS     ("PROTON-NUCLEUS PHYSICS"),
	SPECIAL_OPTICS_PHYSICS     ("SPECIAL OPTICS PHYSICS"),
	COOLDOWN                   ("COOLDOWN"),
	ACCESS                     ("ACCESS"),
	SHUTDOWN                   ("SHUTDOWN"),
	BEAM_SETUP                 ("BEAM SETUP"),


	UNKNOWN("unknown");
	
	private final String code;

	private LHCMachineMode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	/** method to convert a string describing an LHC machine
	 *  mode into an LHCBeamMode enum.
	 *
	 * @return the LHCMachineMode value corresponding to code
	 *  or UNKNOWN if not found.
	 */
	public static LHCMachineMode getModeByCode(String code) {

		// note that some beam mode stirngs have spaces in them
		// so we can't use LHCBeamMode.valueOf(code)
		for (LHCMachineMode mode : LHCMachineMode.values()) {
			if (code.equals(mode.getCode())) {
				return mode;
			}
		}

		// no matching string found
		return LHCMachineMode.UNKNOWN;
	}
}
