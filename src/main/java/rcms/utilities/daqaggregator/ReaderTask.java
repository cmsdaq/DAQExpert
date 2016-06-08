package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.reasoning.base.CheckManager;
import rcms.utilities.daqaggregator.reasoning.base.Entry;
import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

/**
 * 
 * @author Maciej Gladki
 *
 */
public class ReaderTask extends TimerTask {

	private Map<String, File> filesProcessed = new HashMap<>();
	private static final Logger logger = Logger.getLogger(ReaderTask.class);
	int last = 0;

	CheckManager checkManager = new CheckManager();

	private DataResolutionManager dataSegmentator;

	private long dontSendBefore = 0;

	public ReaderTask(DataResolutionManager dataSegmentator, long mostRecentSnapshot) {
		this.dataSegmentator = dataSegmentator;
		dontSendBefore = mostRecentSnapshot;
	}

	@Override
	public void run() {

		try {

			ExpertPersistorManager.get().getUnprocessedSnapshots(filesProcessed, checkManager);
			int all = filesProcessed.size();
			logger.debug("files processed in this round " + (all - last));
			last = all;

			NotificationSender notificationSender = new NotificationSender();

			List<Entry> events = EventProducer.get().getResult();
			List<Entry> recentEvents = new ArrayList<>();
			long latest = dontSendBefore;
			for (Entry event : events) {
				if (event.getStart().getTime() > dontSendBefore && event.isShow()) {
					recentEvents.add(event);
					if (event.getStart().getTime() > latest)
						latest = event.getStart().getTime();

				}
			}

			logger.debug(recentEvents.size() + " notifications can be sent in this round");
			notificationSender.send(recentEvents);
			dontSendBefore = latest;

			dataSegmentator.prepareMultipleResolutionData();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
