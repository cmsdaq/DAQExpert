package rcms.utilities.daqexpert.reasoning.base;

public abstract class EventFinder {
	/**
	 * Name of the event found
	 */
	protected String name;

	/**
	 * Group of the event found
	 */
	protected EventGroup group;

	/**
	 * Priority of the event found
	 */
	protected EventPriority priority;
	
	/**
	 * Condition description
	 */
	protected String description;
	
	/**
	 * Get name of the condition
	 * 
	 * @return name of the condition
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of the condition
	 * 
	 * @param name
	 *            name of the condition
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the group of the condition
	 * 
	 * @return group of the condition
	 */
	public EventGroup getGroup() {
		return group;
	}

	/**
	 * Set the group of the condition
	 * 
	 * @param group
	 *            group of the condition
	 */
	public void setGroup(EventGroup group) {
		this.group = group;
	}

	/**
	 * Get priority of the condition
	 * 
	 * @return priority of the condition
	 */
	public EventPriority getPriority() {
		return priority;
	}

	/**
	 * Set the priority of the condition
	 * 
	 * @param priority
	 *            priority of the condition
	 */
	public void setPriority(EventPriority priority) {
		this.priority = priority;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
