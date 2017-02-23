package rcms.utilities.daqexpert.notifications;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;

/**
 * Sends signals to Notificatin Manager via RESTFull API
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class NotificationSignalSender {

	private static final Logger logger = Logger.getLogger(NotificationSignalSender.class);

	private NotificationSignalConnector notificationConnector;

	private ObjectMapper objectMapper;

	/**
	 * API adress for create signals TODO: merge to api and send parameter
	 */
	private String createAPIAddress;

	/**
	 * API address for finish signals
	 */
	private String finishAPIAddress;

	/**
	 * List of entry id's having start signal sent
	 */
	private Set<Long> sentIds = new HashSet<>();

	private final long appStartTime;

	/**
	 * @param applicationStartTime
	 *            notifications based on older snapshots than this timestamp
	 *            will not generate notifications
	 */
	public NotificationSignalSender(NotificationSignalConnector notificationConnector, String createAPIAddress,
			String finishAPIAddress, long appStartTime) {
		this.notificationConnector = notificationConnector;
		this.appStartTime = appStartTime;
		this.createAPIAddress = createAPIAddress;
		this.finishAPIAddress = finishAPIAddress;
		objectMapper = new ObjectMapper();
		objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

	}

	/**
	 * 
	 * @param entry
	 */
	public void send(Condition entry) {

		if (!isPastSnapshot(entry.getStart().getTime())) {
			if (entry.getLogicModule().getLogicModule().isNotificationDisplay()
					|| entry.getLogicModule().getLogicModule().isNotificationPlay()) {
				EntryState state = entry.getState();
				switch (state) {
				case NEW:
				case MATURE:
					entry.setState(EntryState.STARTED);
					// send started notification
					logger.debug("Send entry notification, id: " + entry.getId());
					sendStartSignal(entry);
					sentIds.add(entry.getId());
					break;
				case STARTED:
					// TODO: send update when there is new information about
					// event
					if (entry.hasChanged())
						logger.info("Send entry update, id: " + entry.getId());
					break;
				case FINISHED:
					// send finished
					if (sentIds.contains(entry.getId())) {
						logger.debug("Send entry finish, id: " + entry);
						sendEndSignal(entry);
						sentIds.remove(entry.getId());
					}
					break;
				default:
					logger.warn("Problem sending signal to NotificationManager: Entry has no state " + entry);
					break;
				}
			} else {

				logger.debug("Event not to show nor to display in NM: " + entry);
			}
		} else {
			logger.debug("Past event, notification cancelled for: " + entry);
		}
	}

	/**
	 * Sends start signal to
	 * 
	 * @param condition
	 * @return
	 */
	private int sendStartSignal(Condition condition) {

		logger.debug("Sending start signal");

		Notification notification = new Notification();
		notification.setDisplay(condition.getLogicModule().getLogicModule().isNotificationDisplay());
		notification.setPlay(condition.getLogicModule().getLogicModule().isNotificationPlay());
		notification.setDate(condition.getStart());

		String message = condition.getLogicModule().getLogicModule().getName();
		if (condition.getLogicModule().getLogicModule().getPrefixToPlay() != null) {
			message = condition.getLogicModule().getLogicModule().getPrefixToPlay() + message;
		}
		if (condition.getLogicModule().getLogicModule().getSuffixToPlay() != null) {
			message = message + condition.getLogicModule().getLogicModule().getSuffixToPlay();
		}
		if (condition.getLogicModule().getLogicModule().getSoundToPlay() != null) {
			notification.setSoundId(condition.getLogicModule().getLogicModule().getSoundToPlay().ordinal());
		}
		if (condition.getLogicModule().getLogicModule().isSkipText()) {
			message = "";
		}
		if (condition.getLogicModule().getLogicModule() instanceof ComparatorLogicModule) {
			notification.setCloseable(false);
		} else {
			notification.setCloseable(true);
		}
		logger.debug("Now working on: " + message);
		logger.debug("Now working on: " + condition);

		if (condition.getLogicModule().getLogicModule() instanceof ContextLogicModule) {
			ContextLogicModule contextLogicModule = (ContextLogicModule) condition.getLogicModule().getLogicModule();
			Context context = contextLogicModule.getContext();
			message = context.getContentWithContext(message);
		}
		if (condition.getLogicModule().getLogicModule() instanceof ActionLogicModule) {

			ActionLogicModule actionLogicModule = (ActionLogicModule) condition.getLogicModule().getLogicModule();
			Context context = actionLogicModule.getContext();
			notification.setAction(context.getActionWithContext(actionLogicModule.getAction()));
		}

		notification.setMessage(message);

		// TODO: type id should indicate expert system
		notification.setType_id(0);
		notification.setId(condition.getId());

		logger.info("To be sent: " + notification.getMessage());

		String notificationString;
		try {
			notificationString = objectMapper.writeValueAsString(notification);
			return notificationConnector.sendSignal(createAPIAddress, notificationString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			logger.error("Could not send", e);
		}
		return 0;

	}

	private void sendEndSignal(Condition entry) {
		FinishNotification finishNotification = new FinishNotification();
		finishNotification.setId(entry.getId());
		finishNotification.setDate(entry.getEnd());
		finishNotification.setDisplay(true);
		finishNotification.setPlay(entry.getLogicModule().getLogicModule().isNotificationEndPlay());
		try {
			String notificationAsString = objectMapper.writeValueAsString(finishNotification);
			notificationConnector.sendSignal(finishAPIAddress, notificationAsString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	private boolean isPastSnapshot(long time) {
		if (time > appStartTime)
			return false;
		return true;
	}

}
