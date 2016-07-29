package rcms.utilities.daqexpert.reasoning.base;

/**
 * Priority of the condition
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum EventPriority {

	defaultt("default"),
	critical("critical"),
	filtered("filtered"),
	filtered_important("filtered-important"),
	important("important");

	private EventPriority(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	private final String code;

}
