package rcms.utilities.daqaggregator.reasoning.base;

/**
 * @author Maciej Gladki
 */
public enum TTSState {

	DISCONNECTED("D_0"), WARNING("W_1"), OUT_OF_SYNC("S_2"), BUSY("B_4"), TTS_READY("R_8"), ERROR("E_c"), UNKNOWN("");

	private TTSState(String code) {
		this.code = code;
	}

	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static TTSState getByCode(String code) {
		if (code == null)
			return TTSState.UNKNOWN;
		if (code.equalsIgnoreCase(TTS_READY.getCode())) {
			return TTSState.TTS_READY;
		} else if (code.equalsIgnoreCase(DISCONNECTED.getCode())) {
			return TTSState.DISCONNECTED;
		} else if (code.equalsIgnoreCase(WARNING.getCode())) {
			return TTSState.WARNING;
		} else if (code.equalsIgnoreCase(OUT_OF_SYNC.getCode())) {
			return TTSState.OUT_OF_SYNC;
		} else if (code.equalsIgnoreCase(BUSY.getCode())) {
			return TTSState.BUSY;
		} else if (code.equalsIgnoreCase(ERROR.getCode())) {
			return TTSState.ERROR;
		}
		return TTSState.UNKNOWN;
	}

}
