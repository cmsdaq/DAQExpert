package rcms.utilities.daqexpert.reasoning.base.enums;

/**
 * Priority of the condition
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum EventPriority {

	DEFAULTT("default"),
	IMPORTANT("important"),
	WARNING("warning"),
	CRITICAL("critical"),
	FILTERED("filtered"),
	EXPERIMENTAL("experimental"),
	FILTERED_IMPORTANT("filtered-important");

	private EventPriority(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	private final String code;

}
