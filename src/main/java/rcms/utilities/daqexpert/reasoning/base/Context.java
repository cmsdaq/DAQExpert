package rcms.utilities.daqexpert.reasoning.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.action.Action;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

@SuppressWarnings("serial")
public class Context implements Serializable {

	private static final Logger logger = Logger.getLogger(Context.class);

	private Map<String, Set<Object>> context;

	private Set<String> actionKey;

	public Context() {
		this.context = new HashMap<>();
		this.actionKey = new HashSet<>();
	}

	public void register(String key, Object object) {
		if (!context.containsKey(key)) {
			context.put(key, new HashSet<Object>());
		}

		if (!context.get(key).contains(object)) {
			context.get(key).add(object);
		}

	}

	/**
	 * Called when Condition ends
	 */
	public void clearContext() {
		this.context = new HashMap<>();
		this.actionKey = new HashSet<>();
	}

	public Map<String, Set<Object>> getContext() {
		return this.context;
	}

	@Override
	public String toString() {
		return "ContextCollector [context=" + context + "]";
	}

	public List<String> getActionWithContext(Action actionn) {

		List<String> actionSteps = null;

		if (actionn instanceof ConditionalAction) {
			ConditionalAction action = (ConditionalAction) actionn;
			actionSteps = action.getContextSteps(getActionKey());
		} else if (actionn instanceof SimpleAction) {
			actionSteps = actionn.getSteps();
		}
		logger.debug("Putting context into action: " + actionSteps);
		logger.debug("Context to be used: " + context);

		if (actionSteps != null) {
			List<String> actionStepsWithContext = new ArrayList<>();

			for (String step : actionSteps) {
				actionStepsWithContext.add(putContext(step));
			}

			return actionStepsWithContext;
		}
		return null;
	}

	/**
	 * Put collected context into given text. All variables {{VARIABLE_NAME}}
	 * will be replaced with value if exists in context or ? sign
	 * 
	 * @param input
	 *            text where context will be inserted
	 * @return copy of the text with context inserted
	 */
	private String putContext(String input) {
		ObjectMapper mapper = new ObjectMapper();
		String output = new String(input);

		for (java.util.Map.Entry<String, Set<Object>> entry : this.getContext().entrySet()) {

			String variableKeyNoRgx = "{{" + entry.getKey() + "}}";
			String variableKeyRegex = "\\{\\{" + entry.getKey() + "\\}\\}";

			if (output.contains(variableKeyNoRgx)) {

				String replacement;
				if (entry.getValue().size() == 1)
					replacement = entry.getValue().iterator().next().toString();
				else {
					if (entry.getValue().size() > 8) {
						replacement = "(" + entry.getValue().stream().limit(7).map(i -> i.toString())
								.collect(Collectors.joining(", "));
						replacement += " and " + (entry.getValue().size() - 7) + " more)";
					} else {
						replacement = entry.getValue().stream().map(i -> i.toString())
								.collect(Collectors.joining(", "));
					}
				}
				output = output.replaceAll(variableKeyRegex, replacement);

			} else {
				logger.debug("No key " + variableKeyNoRgx + " in " + output);
			}
		}

		return output;
	}

	public String getContentWithContext(String message) {

		logger.debug("Putting context into message: " + message);
		logger.debug("Context to be used: " + context);

		String newMessage = putContext(message);

		logger.debug("Message with context: " + newMessage);

		return newMessage;
	}

	public String getActionKey() {
		if (actionKey.size() == 1)
			return actionKey.iterator().next();
		else
			return null;
	}

	public void setActionKey(String actionKey) {
		this.actionKey.add(actionKey);
	}

}
