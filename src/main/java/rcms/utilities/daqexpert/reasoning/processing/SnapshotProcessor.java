package rcms.utilities.daqexpert.reasoning.processing;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import oracle.net.aso.e;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.notifications.NotificationSignalConnector;
import rcms.utilities.daqexpert.notifications.NotificationSignalSender;
import rcms.utilities.daqexpert.reasoning.base.Entry;

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

	private EventProducer eventProducer;

	public SnapshotProcessor(EventProducer eventProducer) {

		this.eventProducer = eventProducer;
		NotificationSignalConnector notificationConnector = new NotificationSignalConnector();

		long offset = 0;
		try {
			offset = Long.parseLong(Application.get().getProp().getProperty(Application.NM_OFFSET));
		} catch (NumberFormatException e) {
			logger.error("Problem parsing offset");
		}

		this.notificationSender = new NotificationSignalSender(notificationConnector,
				Application.get().getProp().getProperty(Application.NM_API_CREATE),
				Application.get().getProp().getProperty(Application.NM_API_CLOSE), System.currentTimeMillis() - offset);
		this.checkManager = new LogicModuleManager(eventProducer);
	}

	public Set<Entry> process(DAQ daqSnapshot, boolean createNotifications, boolean includeExperimental) {
		logger.trace("Process snapshot " + new Date(daqSnapshot.getLastUpdate()));
		Set<Entry> result = new LinkedHashSet<>();
		try {
			List<Entry> lmResults = checkManager.runLogicModules(daqSnapshot, includeExperimental);

			for (Entry lmResult : lmResults) {
				result.add(lmResult);
			}

			// Application.get().getDataManager().getResult().addAll(result);

			logger.debug("Results from CheckManager for this snapshot: " + lmResults);

			if (createNotifications) {
				if (result.size() != 0) {
					logger.debug("Creating notifications for " + result);
					for (Entry entry : result)
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

	public EventProducer getEventProducer() {
		return eventProducer;
	}

	public void clearProducer() {
		eventProducer.clearProducer();// = new EventProducer();
	}

	public LogicModuleManager getCheckManager() {
		return checkManager;
	}

}
