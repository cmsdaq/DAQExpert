package rcms.utilities.daqexpert.events;

public enum EventType {

	Single("Single", "Single independent event"),
	ConditionStart("Start", "Event indicating start of the condition"),
	ConditionUpdate("Update", "Event indicating update of the condition"),
	ConditionEnd("End", "Event indicating end of the condition");

	private final String description;

	private final String name;

	public String getDescription() {
		return description;
	}

	private EventType(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

}
