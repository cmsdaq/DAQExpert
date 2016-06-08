package rcms.utilities.daqaggregator.reasoning.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonPropertyOrder({ "name", "description", "action", "elements" })
public class EventRaport {

	private String description;

	private String name;

	private String action;

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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void initialize(String name, String description, String action) {
		this.name = name;
		this.description = description;
		this.action = action;
		this.initialized = true;
	}

	@Override
	public String toString() {
		return "EventRaport [description=" + description + ", name=" + name + ", action=" + action + ", elements="
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
