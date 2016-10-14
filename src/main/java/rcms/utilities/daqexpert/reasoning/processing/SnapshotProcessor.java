package rcms.utilities.daqexpert.reasoning.processing;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

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

	private LogicModuleManager checkManager;

	private static final Logger logger = Logger.getLogger(SnapshotProcessor.class);

	private final NotificationSignalSender notificationSender;

	public SnapshotProcessor(EventProducer eventProducer) {

		NotificationSignalConnector notificationConnector = new NotificationSignalConnector();

		this.notificationSender = new NotificationSignalSender(notificationConnector,
				Application.get().getProp().getProperty(Application.NM_API_CREATE),
				Application.get().getProp().getProperty(Application.NM_API_CLOSE), System.currentTimeMillis());
		this.checkManager = new LogicModuleManager(eventProducer);
	}

	public Set<Entry> process(DAQ daqSnapshot, boolean createNotifications) {
		logger.trace("Process snapshot");
		List<Entry> lmResults = checkManager.runLogicModules(daqSnapshot);

		Set<Entry> result = new LinkedHashSet<>();
		for (Entry lmResult : lmResults) {
			result.add(lmResult);
		}

		// Application.get().getDataManager().getResult().addAll(result);

		logger.debug("Results from CheckManager for this snapshot: " + lmResults);

		if (createNotifications)
			for (Entry entry : result)
				if (entry.isShow())
					notificationSender.send(entry);
		return result;
	}

}
