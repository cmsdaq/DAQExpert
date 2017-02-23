package rcms.utilities.daqexpert.reasoning.processing;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.notifications.NotificationSignalConnector;
import rcms.utilities.daqexpert.notifications.NotificationSignalSender;
import rcms.utilities.daqexpert.persistence.Condition;

/**
 * Processes snapshot in analysis
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SnapshotProcessor {

	private final LogicModuleManager checkManager;

	private static final Logger logger = Logger.getLogger(SnapshotProcessor.class);

	private final NotificationSignalSender notificationSender;

	private ConditionProducer eventProducer;

	public SnapshotProcessor(ConditionProducer eventProducer) {

		this.eventProducer = eventProducer;
		NotificationSignalConnector notificationConnector = new NotificationSignalConnector();

		long offset = 0;
		try {
			offset = Long.parseLong(Application.get().getProp(Setting.NM_OFFSET));
		} catch (NumberFormatException e) {
			logger.error("Problem parsing offset");
		}
		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long startTime = utcCalendar.getTimeInMillis() - offset;
		utcCalendar.setTimeInMillis(startTime);
		Date startDate = utcCalendar.getTime();
		String offsetString = DurationFormatUtils.formatDuration(offset, "d 'days', HH:mm:ss", true);
		logger.info("Notifications will generated from: " + startDate + " (now minus offset of " + offsetString + ")");

		this.notificationSender = new NotificationSignalSender(notificationConnector,
				Application.get().getProp(Setting.NM_API_CREATE),
				Application.get().getProp(Setting.NM_API_CLOSE), System.currentTimeMillis() - offset);
		this.checkManager = new LogicModuleManager(eventProducer);
	}

	public Set<Condition> process(DAQ daqSnapshot, boolean createNotifications, boolean includeExperimental) {
		logger.trace("Process snapshot " + new Date(daqSnapshot.getLastUpdate()));
		Set<Condition> result = new LinkedHashSet<>();
		try {
			List<Condition> lmResults = checkManager.runLogicModules(daqSnapshot, includeExperimental);

			for (Condition lmResult : lmResults) {
				result.add(lmResult);
			}

			// Application.get().getDataManager().getResult().addAll(result);

			logger.debug("Results from CheckManager for this snapshot: " + lmResults);

			if (createNotifications) {
				if (result.size() != 0) {
					logger.debug("Creating notifications for " + result);
					for (Condition entry : result)
						if (entry.isShow()) {
							logger.debug("Send notification for " + entry);
							notificationSender.send(entry);
						} else {
							logger.debug("No notification (show=false) for result " + entry);
						}
				}
			} else {
				logger.debug("Global notification flag set to false for this results, notification will not be send: "
						+ result);
			}
		} catch (RuntimeException e) {
			logger.error("Exception processing snapshot", e);
		}
		return result;
	}

	public ConditionProducer getEventProducer() {
		return eventProducer;
	}

	public void clearProducer() {
		eventProducer.clearProducer();// = new EventProducer();
	}

	public LogicModuleManager getCheckManager() {
		return checkManager;
	}

}
