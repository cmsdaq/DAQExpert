package rcms.utilities.daqexpert.websocket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
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

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	private final ConditionDashboard conditionDashboard;

	private final Set<Long> createdIds = new HashSet<>();

	public ConditionSessionHandler(ConditionDashboard conditionDashboard) {
		this.conditionDashboard = conditionDashboard;
		conditionDashboard.setSessionHander(this);
	}

	private final Set<Session> sessions = new HashSet<>();

	/**
	 * Add new session - called on page load
	 * 
	 * @param session
	 */
	public void addSession(Session session) {
		sessions.add(session);
		logger.info("New session connected: " + session);
		for (Condition condition : conditionDashboard.getFilteredCurrentConditions()) {
			createdIds.add(condition.getId());
			JsonObject addMessage = createAddRecentMessage(condition);
			sendToSession(session, addMessage);
		}

		if (conditionDashboard.getCurrentCondition() != null) {
			logger.info("New session will have info about current condition");
			JsonObject addMessage = createCurrentMessage(conditionDashboard.getCurrentCondition());
			sendToSession(session, addMessage);
		} else {
			JsonProvider provider = JsonProvider.provider();
			JsonObject removeMessage = provider.createObjectBuilder().add("action", "removeCurrent").build();
			sendToAllConnectedSessions(removeMessage);
		}
	}

	/**
	 * Remove ression - called when the browser is turned off
	 * 
	 * @param session
	 */
	public void removeSession(Session session) {
		sessions.remove(session);
	}

	/**
	 * Action to remove current suggestion
	 */
	public void removeCurrent() {
		logger.info("Removing current");
		JsonProvider provider = JsonProvider.provider();
		JsonObject removeMessage = provider.createObjectBuilder().add("action", "removeCurrent").build();
		sendToAllConnectedSessions(removeMessage);

		addRecent();

	}

	/*
	 * public void updateCurrent(Condition condition) {
	 * 
	 * if (currentCondition == null) { currentCondition = condition; JsonObject
	 * addMessage = createCurrentMessage(condition);
	 * sendToAllConnectedSessions(addMessage); } else { logger.info(
	 * "Exists some suggestion, TODO: update it condition changed"); } }
	 */

	public void setCurrent(Condition condition) {
		logger.info("Setting current");
		JsonObject addMessage = createCurrentMessage(condition);
		sendToAllConnectedSessions(addMessage);
	}

	/**
	 * Action to add new recent condition
	 * 
	 * @param condition
	 */
	public void addRecent() {

		logger.info("Adding recents");
		// Note that it may result in empty. condition may be current

		for (Condition condition : conditionDashboard.getFilteredCurrentConditions()) {
			if (!createdIds.contains(condition.getId())) {
				logger.info("-> adding recent " + condition.getId());
				JsonObject addMessage = createAddRecentMessage(condition);
				sendToAllConnectedSessions(addMessage);
				createdIds.add(condition.getId());
			}
		}
	}

	public void removeRecent(Long id) {
		logger.info("Removing recent " + id);
		createdIds.remove(id);
		JsonProvider provider = JsonProvider.provider();
		JsonObject removeMessage = provider.createObjectBuilder().add("action", "remove").add("id", id).build();
		sendToAllConnectedSessions(removeMessage);
	}

	/**
	 * Create message
	 * 
	 * @param condition
	 * @return
	 */
	private JsonObject createCurrentMessage(Condition condition) {
		JsonProvider provider = JsonProvider.provider();
		logger.info("Creating current condition message for : " + condition);

		String message = condition.getActionSteps() != null ? condition.getActionSteps().toString() : "";
		String description = condition.getDescription() != null ? condition.getDescription() : "";
		String title = condition.getTitle() != null ? condition.getTitle() + " #" + condition.getId() : "";
		String duration = condition.getEnd() == null ? "Ongoing" : "finished";
		JsonArrayBuilder actionArrayBuilder = provider.createArrayBuilder();
		if (condition.getActionSteps() != null) {
			for (String step : condition.getActionSteps()) {
				actionArrayBuilder.add(step);
			}
		}
		JsonArray actionArray = actionArrayBuilder.build();

		JsonObject addMessage = provider.createObjectBuilder().add("action", "addSuggestion")
				.add("id", condition.getId()).add("name", title).add("type", condition.getStart().toString())
				.add("status", message).add("description", description).add("duration", duration)
				.add("steps", actionArray).build();

		logger.info("Created message for current condition: " + addMessage);
		return addMessage;
	}

	/**
	 * Create message
	 * 
	 * @param condition
	 * @return
	 */
	private JsonObject createUpdateMessage(Condition condition) {
		JsonProvider provider = JsonProvider.provider();
		logger.debug("Creating update for condition: " + condition);
		/*
		 * JsonObject updateMessage =
		 * provider.createObjectBuilder().add("action", "update").add("id",
		 * condition.getId()) .add("status", condition.getEnd()).build();
		 */

		// logger.debug("Created message for event: " + updateMessage);
		// return updateMessage;
		return null;
	}

	/**
	 * Create message
	 * 
	 * @param condition
	 * @return
	 */
	private JsonObject createAddRecentMessage(Condition condition) {
		JsonProvider provider = JsonProvider.provider();
		logger.debug("Creating message for event: " + condition);

		String message = condition.getActionSteps() != null ? condition.getActionSteps().toString() : "";
		String tts = condition.getDescription() != null ? condition.getDescription() : "";
		String title = condition.getTitle() != null ? condition.getTitle() + "#" + condition.getId() : "";
		String duration = condition.getEnd() == null ? "Ongoing" : "finished";

		JsonObject addMessage = provider.createObjectBuilder().add("action", "add").add("id", condition.getId())
				.add("name", title).add("type", dateFormat.format(condition.getStart())).add("status", tts)
				.add("description", message).add("duration", duration).build();

		logger.debug("Created message for event: " + addMessage);
		return addMessage;
	}

	/**
	 * Send message to all connected sessions
	 * 
	 * @param message
	 */
	private void sendToAllConnectedSessions(JsonObject message) {
		for (Session session : sessions) {
			sendToSession(session, message);
		}
	}

	/**
	 * Send message to session
	 * 
	 * @param session
	 * @param message
	 */
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