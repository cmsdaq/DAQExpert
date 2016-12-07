package rcms.utilities.daqexpert.notifications;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.base.Entry;
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
	public void send(Entry entry) {

		if (!isPastSnapshot(entry.getStart().getTime())) {
			if (entry.getEventFinder().isNotificationDisplay() || entry.getEventFinder().isNotificationPlay()) {
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
	 * @param event
	 * @return
	 */
	private int sendStartSignal(Entry event) {

		logger.debug("Sending start signal");

		Notification notification = new Notification();
		notification.setDisplay(event.getEventFinder().isNotificationDisplay());
		notification.setPlay(event.getEventFinder().isNotificationPlay());
		notification.setDate(event.getStart());

		String message = event.getEventFinder().getName();
		if (event.getEventFinder().getPrefixToPlay() != null) {
			message = event.getEventFinder().getPrefixToPlay() + message;
		}
		if (event.getEventFinder().getSuffixToPlay() != null) {
			message = message + event.getEventFinder().getSuffixToPlay();
		}
		if (event.getEventFinder().getSoundToPlay() != null) {
			notification.setSoundId(event.getEventFinder().getSoundToPlay().ordinal());
		}
		if (event.getEventFinder().isSkipText()) {
			message = "";
		}
		if (event.getEventFinder() instanceof ComparatorLogicModule) {
			notification.setCloseable(false);
		} else {
			notification.setCloseable(true);
		}
		logger.debug("Now working on: " + message);
		logger.debug("Now working on: " + event);

		if (event.getEventFinder() instanceof ActionLogicModule) {

			ActionLogicModule finder = (ActionLogicModule) event.getEventFinder();
			Context context = ((ActionLogicModule) finder).getContext();
			message = context.getMessageWithContext(message);
			notification.setAction(context.getActionWithContext(finder.getAction()));
		}

		notification.setMessage(message);
		notification.setType_id(event.getEventFinder().getGroup().getNmId());
		notification.setId(event.getId());

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

	private void sendEndSignal(Entry entry) {
		FinishNotification finishNotification = new FinishNotification();
		finishNotification.setId(entry.getId());
		finishNotification.setDate(entry.getEnd());
		finishNotification.setDisplay(true);
		finishNotification.setPlay(entry.getEventFinder().isNotificationEndPlay());
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
