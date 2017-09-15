package rcms.utilities.daqexpert.websocket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;

public class ConditionSessionHandler {

	/** Class logger */
	private static final Logger logger = Logger.getLogger(ConditionSessionHandler.class);

	/** Format of date for JSON messages */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** Model - has all conditions that are active at any moment */
	private final ConditionDashboard conditionDashboard;

	/** Connected sessions */
	private final Set<Session> sessions = new HashSet<>();

	/** Constructor */
	public ConditionSessionHandler(ConditionDashboard conditionDashboard) {
		this.conditionDashboard = conditionDashboard;
		conditionDashboard.setSessionHander(this);
	}

	/**
	 * Handler for change of dominating condition
	 * 
	 * @param dominatingCondition
	 *            condition that is now dominating
	 */
	public void handleDominatingConditionChange(Condition dominatingCondition) {
		logger.info("Setting current");
		JsonObject addMessage = createSelectMessage(dominatingCondition);
		sendToAllConnectedSessions(addMessage);
	}

	/**
	 * Handler for condition update
	 * 
	 * @param updatedCondition
	 *            updated condition
	 */
	public void handleConditionUpdate(Condition updatedCondition) {
		logger.info("Updating");
		JsonObject addMessage = createUpdateMessage(updatedCondition);
		sendToAllConnectedSessions(addMessage);
	}

	/**
	 * Handler for recent conditions list change
	 * 
	 * @param recentConditions
	 *            collection of recent conditions
	 */
	public void handleRecentConditionsChange(Collection<Condition> recentConditions) {
		JsonObject addMessage = createAddMessage(recentConditions);
		sendToAllConnectedSessions(addMessage);
	}

	/**
	 * Add new session - called when there is new web-socked connection
	 * 
	 * @param session
	 *            session that has been connected
	 */
	public void addSession(Session session) {
		sessions.add(session);
		logger.info("New session connected: " + session);

		if (conditionDashboard.getCurrentCondition() != null) {
			logger.info("New session will have info about current condition");
			JsonObject selectMessage = createSelectMessage(conditionDashboard.getCurrentCondition());
			sendToSession(session, selectMessage);
		}

		JsonObject addMessage = createAddMessage(conditionDashboard.getCurrentConditions());
		sendToSession(session, addMessage);

	}

	/**
	 * Remove session - called when the web-socked disconnects
	 * 
	 * @param session
	 *            session that has been disconnected
	 */
	public void removeSession(Session session) {
		sessions.remove(session);
	}

	/**
	 * Create message for adding multiple conditions
	 * 
	 * @param condition
	 * @return
	 */
	private JsonObject createAddMessage(Collection<Condition> conditions) {
		JsonProvider provider = JsonProvider.provider();
		logger.debug("Creating message for conditions: " + conditions.size());

		JsonArrayBuilder objectsBuilder = provider.createArrayBuilder();

		for (Condition condition : conditions) {
			JsonObjectBuilder objectBuilder = provider.createObjectBuilder();
			
			objectBuilder.add("id", condition.getId());
			objectBuilder.add("title", condition.getTitle());
			objectBuilder.add("timestamp", dateFormat.format(condition.getStart()));
			objectBuilder.add("announced", false);

			if (condition.getActionSteps() != null){
				JsonArrayBuilder actions = provider.createArrayBuilder();
				for(String step: condition.getActionSteps()){
					actions.add(step);
				}
				objectBuilder.add("action", actions);
			}

			if (condition.getDescription() != null)
				objectBuilder.add("description", condition.getDescription());

			if (condition.getEnd() == null){
				objectBuilder.add("status", "ongoing");
			} else{
				condition.calculateDuration();
				objectBuilder.add("duration", condition.getDuration());
				objectBuilder.add("status", "finished");
			}
			
			JsonObject object = objectBuilder.build();

			objectsBuilder.add(object);
		}
		JsonObject message = provider.createObjectBuilder().add("action", "add").add("objects", objectsBuilder.build())
				.build();

		logger.debug("Created message for event: " + message);
		return message;
	}

	/**
	 * Create select message indicating dominating condition
	 * 
	 * @param condition
	 * @return
	 */
	private JsonObject createSelectMessage(Condition condition) {
		JsonProvider provider = JsonProvider.provider();
		logger.debug("Creating select for condition: " + condition);
		Long id = 0L;
		if (condition != null) {
			id = condition.getId();
		}
		JsonObject updateMessage = provider.createObjectBuilder().add("action", "select").add("id", id).build();
		logger.debug("Created select message for event: " + updateMessage);
		return updateMessage;
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

		JsonObjectBuilder builder = provider.createObjectBuilder().add("id", condition.getId());
		if (condition.getEnd() != null) {
			builder.add("status", "finished");
			condition.calculateDuration();
			builder.add("duration", condition.getDuration());
		} else {
			builder.add("status", "updated");
		}

		JsonObject object = builder.build();
		JsonObject updateMessage = provider.createObjectBuilder().add("action", "update").add("object", object).build();

		logger.debug("Created message for event: " + updateMessage);
		return updateMessage;
	}

	/**
	 * Send message of any type to all connected sessions
	 * 
	 * @param message
	 */
	private void sendToAllConnectedSessions(JsonObject message) {
		for (Session session : sessions) {
			sendToSession(session, message);
		}
	}

	/**
	 * Send message of any type to particular session
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