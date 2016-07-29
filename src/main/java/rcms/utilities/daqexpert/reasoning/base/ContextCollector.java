package rcms.utilities.daqexpert.reasoning.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContextCollector implements Serializable{
	
	private static final Logger logger = Logger.getLogger(ContextCollector.class);

	private Map<String, Set<Object>> context;

	public ContextCollector() {
		this.context = new HashMap<>();
	}

	public void register(String key, Object object) {
		if (!context.containsKey(key)) {
			context.put(key, new HashSet<Object>());
		}

		if (!context.get(key).contains(object)) {
			context.get(key).add(object);
		}
	}

	public void clearContext() {
		this.context = new HashMap<>();
	}

	public Map<String, Set<Object>> getContext() {
		return this.context;
	}

	@Override
	public String toString() {
		return "ContextCollector [context=" + context + "]";
	}
	
	public String getMessageWithContext(String message) {

		logger.info("Putting context into message: " + message);
		logger.info("Context to be used: " + context);
		ObjectMapper mapper = new ObjectMapper();
		String newMessage = new String(message);

		for (java.util.Map.Entry<String, Set<Object>> entry : this.getContext().entrySet()) {
			String replacement;
			try {
				if (entry.getValue().size() == 1)
					replacement = entry.getValue().iterator().next().toString();
				else {
					if (entry.getValue().size() > 3) {
						replacement = "[" + entry.getValue().iterator().next().toString() + " and "
								+ (entry.getValue().size() - 1) + " more]";
					} else {
						replacement = mapper.writeValueAsString(entry.getValue());
					}
				}
				newMessage = newMessage.replaceAll("\\{\\{" + entry.getKey() + "\\}\\}", replacement);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		logger.info("Message with context: " + newMessage);

		return newMessage;
	}

}
