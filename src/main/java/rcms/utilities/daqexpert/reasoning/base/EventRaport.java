package rcms.utilities.daqexpert.reasoning.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * General purpose event raport.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonPropertyOrder({ "name", "description", "action", "elements" })
public class EventRaport {

	private String description;

	private String name;

	private List<String> actionSteps;

	@JsonIgnore
	private boolean initialized;

	private final Map<String, Set<Object>> elements = new HashMap<>();

	public EventRaport() {
		initialized = false;
	}

	public Set<Object> getSetByCode(String code) {
		if (!elements.containsKey(code))
			elements.put(code, new HashSet<>());
		return elements.get(code);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAction() {
		return actionSteps;
	}

	public void setAction(List<String> action) {
		this.actionSteps = action;
	}

	public void initialize(String name, String description, List<String> action) {
		this.name = name;
		this.description = description;
		this.actionSteps = action;
		this.initialized = true;
	}

	@Override
	public String toString() {
		return "EventRaport [description=" + description + ", name=" + name + ", action=" + actionSteps + ", elements="
				+ elements + "]";
	}

	public Map<String, Set<Object>> getElements() {
		return elements;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}
