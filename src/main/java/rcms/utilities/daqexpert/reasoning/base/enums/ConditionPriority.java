package rcms.utilities.daqexpert.reasoning.base.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Priority of the condition
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum ConditionPriority {

	DEFAULTT("default"),
	IMPORTANT("important"),
	WARNING("warning"),
	CRITICAL("critical"),
	FILTERED("filtered"),
	EXPERIMENTAL("experimental"),
	FILTERED_IMPORTANT("filtered-important");

	private ConditionPriority(String code) {
		this.code = code;
	}

	@JsonValue
	public String getCode() {
		return code;
	}

	private final String code;

}
