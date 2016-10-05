package rcms.utilities.daqexpert.reasoning.base;

import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.notifications.NotificationSignalConnector;
import rcms.utilities.daqexpert.notifications.NotificationSignalSender;

/**
 * Processes snapshot in analysis
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SnapshotProcessor {

	private CheckManager checkManager;

	private static final Logger logger = Logger.getLogger(SnapshotProcessor.class);

	private final NotificationSignalSender notificationSender;

	public SnapshotProcessor(EventProducer eventProducer) {

		NotificationSignalConnector notificationConnector = new NotificationSignalConnector();

		this.notificationSender = new NotificationSignalSender(notificationConnector,
				Application.get().getProp().getProperty(Application.NM_API_CREATE),
				Application.get().getProp().getProperty(Application.NM_API_CLOSE), System.currentTimeMillis());
		this.checkManager = new CheckManager(eventProducer);
	}

	public int process(DAQ daqSnapshot, boolean createNotifications) {
		List<Entry> result = checkManager.runLogicModules(daqSnapshot);

		logger.debug("Results from CheckManager for this snapshot: " + result);

		if (createNotifications)
			for (Entry entry : result)
				if (entry.isShow())
					notificationSender.send(entry);
		return result.size();
	}

}
