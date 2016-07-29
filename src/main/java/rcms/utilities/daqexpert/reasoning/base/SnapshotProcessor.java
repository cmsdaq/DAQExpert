package rcms.utilities.daqexpert.reasoning.base;

import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.NotificationSender;

/**
 * Processes snapshot in analysis
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SnapshotProcessor {

	CheckManager checkManager = new CheckManager();

	private static final Logger logger = Logger.getLogger(SnapshotProcessor.class);

	private final NotificationSender notificationSender;

	public SnapshotProcessor() {
		notificationSender = new NotificationSender();
	}

	public void process(DAQ daqSnapshot) {
		List<Entry> result = checkManager.runLogicModules(daqSnapshot);

		logger.debug("Results from CheckManager for this snapshot: " + result);

		for (Entry entry : result)
			if (entry.isShow())
				notificationSender.rtSend(entry);
	}

}
