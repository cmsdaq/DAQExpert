package rcms.utilities.daqexpert.notifications;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.reasoning.base.ContextCollector;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EntryState;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;

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
	public NotificationSignalSender(NotificationSignalConnector notificationConnector, String createAPIAddress, String finishAPIAddress,
			long appStartTime) {
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

		if ("critical".equals(entry.getClassName()) && !isPastSnapshot(entry.getStart().getTime())) {
			EntryState state = entry.getState();
			switch (state) {
			case NEW:
				// Immediately in 2nd snapshot becomes mature
				// may check date here
				entry.setState(EntryState.MATURE);
				break;
			case MATURE:
				entry.setState(EntryState.STARTED);
				// send started notification
				logger.info("Send entry notification, id: " + entry.getId());
				sendStartSignal(entry);
				sentIds.add(entry.getId());
				break;
			case STARTED:
				// TODO: send update when there is new information about event
				if (entry.hasChanged())
					logger.info("Send entry update, id: " + entry.getId());
				break;
			case FINISHED:
				// send finished
				if (sentIds.contains(entry.getId())) {
					logger.info("Send entry finish, id: " + entry);
					sendEndSignal(entry);
					sentIds.remove(entry.getId());
				}
				break;
			default:
				logger.warn("Problem sending signal to NotificationManager: Entry has no state " + entry);
				break;
			}
		}
	}

	/**
	 * Sends start signal to
	 * 
	 * @param event
	 * @return
	 */
	private int sendStartSignal(Entry event) {

		if (event.getEventFinder() instanceof ExtendedCondition) {

			ExtendedCondition finder = (ExtendedCondition) event.getEventFinder();
			Notification notification = new Notification();
			notification.setDate(event.getStart());

			String message = finder.getDescription();

			logger.info("Now working on: " + message);
			logger.info("Now working on: " + event);

			if (finder instanceof ExtendedCondition) {
				ContextCollector context = ((ExtendedCondition) finder).getContext();
				message = context.getMessageWithContext(message);
				notification.setAction(context.getActionWithContext(finder.getAction()));
			}

			notification.setMessage(message);
			notification.setType_id(event.getEventFinder().getGroup().getNmId());
			notification.setId(event.getId());

			logger.info("To be sent: " + notification);

			String notificationString;
			try {
				notificationString = objectMapper.writeValueAsString(notification);
				return notificationConnector.sendSignal(createAPIAddress, notificationString);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return 0;

	}

	private void sendEndSignal(Entry entry) {
		FinishNotification finishNotification = new FinishNotification();
		finishNotification.setId(entry.getId());
		finishNotification.setDate(entry.getEnd());
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
