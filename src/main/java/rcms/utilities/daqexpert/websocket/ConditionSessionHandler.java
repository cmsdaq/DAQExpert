package rcms.utilities.daqexpert.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;

public class ConditionSessionHandler {

	private static final Logger logger = Logger.getLogger(ConditionSessionHandler.class);

	private final Set<Session> sessions = new HashSet<>();
	private final HashMap<Long, Condition> conditions = new LinkedHashMap<>();

	private Condition currentCondition = null;

	public void addSession(Session session) {
		sessions.add(session);
		logger.info("New session connected: " + session);
		for (Condition event : conditions.values()) {
			JsonObject addMessage = createAddMessage(event);
			sendToSession(session, addMessage);
		}

		if (currentCondition != null) {
			logger.info("New session will have info about current condition");
			JsonObject addMessage = createCurrentMessage(currentCondition);
			sendToSession(session, addMessage);
		} else{
			JsonProvider provider = JsonProvider.provider();
			JsonObject removeMessage = provider.createObjectBuilder().add("action", "removeCurrent").build();
			sendToAllConnectedSessions(removeMessage);
		}
	}

	public void removeSession(Session session) {
		sessions.remove(session);
	}

	public Collection<Condition> getEvents() {
		return conditions.values();
	}

	public void removeCurrent() {
		if (currentCondition != null && currentCondition.getEnd() != null) {
			JsonProvider provider = JsonProvider.provider();
			JsonObject removeMessage = provider.createObjectBuilder().add("action", "removeCurrent").build();
			sendToAllConnectedSessions(removeMessage);
			currentCondition = null;
		}
	}

	public void updateCurrent(Condition condition) {

		if (currentCondition == null) {
			currentCondition = condition;
			JsonObject addMessage = createCurrentMessage(condition);
			sendToAllConnectedSessions(addMessage);
		} else {
			logger.info("Exists some suggestion, TODO: update it condition changed");
		}
	}

	public void addCondition(Condition condition) {
		if (!conditions.containsKey(condition.getId())) {

			if (conditions.size() >= 4) {
				removeEvent(conditions.values().iterator().next());
			}
			conditions.put(condition.getId(), condition);
			JsonObject addMessage = createAddMessage(condition);
			sendToAllConnectedSessions(addMessage);
		}
	}

	public void removeEvent(Condition event) {
		if (event != null) {
			conditions.remove(event.getId());
			JsonProvider provider = JsonProvider.provider();
			JsonObject removeMessage = provider.createObjectBuilder().add("action", "remove").add("id", event.getId())
					.build();
			sendToAllConnectedSessions(removeMessage);
		}
	}

	private JsonObject createCurrentMessage(Condition condition) {
		JsonProvider provider = JsonProvider.provider();
		logger.info("Creating current condition message for : " + condition);

		String message = condition.getActionSteps() != null ? condition.getActionSteps().toString() : "";
		String description = condition.getDescription() != null ? condition.getDescription() : "";
		String title = condition.getTitle() != null ? condition.getTitle() + "#" + condition.getId() : "";
		String duration = condition.getEnd() == null ? "Ongoing" : "finished";
		JsonArrayBuilder actionArrayBuilder = provider.createArrayBuilder();
		for (String step : condition.getActionSteps()) {
			actionArrayBuilder.add(step);
		}
		JsonArray actionArray = actionArrayBuilder.build();

		JsonObject addMessage = provider.createObjectBuilder().add("action", "addSuggestion")
				.add("id", condition.getId()).add("name", title).add("type", condition.getStart().toString())
				.add("status", message).add("description", description).add("duration", duration)
				.add("steps", actionArray).build();

		logger.info("Created message for current condition: " + addMessage);
		return addMessage;
	}

	private JsonObject createAddMessage(Condition condition) {
		JsonProvider provider = JsonProvider.provider();
		logger.debug("Creating message for event: " + condition);

		String message = condition.getActionSteps() != null ? condition.getActionSteps().toString() : "";
		String tts = condition.getDescription() != null ? condition.getDescription() : "";
		String title = condition.getTitle() != null ? condition.getTitle() + "#" + condition.getId() : "";
		String duration = condition.getEnd() == null ? "Ongoing" : "finished";

		JsonObject addMessage = provider.createObjectBuilder().add("action", "add").add("id", condition.getId())
				.add("name", title).add("type", condition.getStart().toString()).add("status", tts)
				.add("description", message).add("duration", duration).build();

		logger.debug("Created message for event: " + addMessage);
		return addMessage;
	}

	private void sendToAllConnectedSessions(JsonObject message) {
		for (Session session : sessions) {
			sendToSession(session, message);
		}
	}

	private void sendToSession(Session session, JsonObject message) {
		try {
			session.getBasicRemote().sendText(message.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
			sessions.remove(session);
			logger.error(ex);
		}
	}
}