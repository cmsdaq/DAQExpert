package rcms.utilities.daqexpert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.reasoning.base.CheckManager;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventProducer;

/**
 * 
 * @author Maciej Gladki
 *
 */
public class ReaderTask extends TimerTask {

	private Map<String, File> filesProcessed = new HashMap<>();
	private static final Logger logger = Logger.getLogger(ReaderTask.class);
	int last = 0;


	private DataResolutionManager dataSegmentator;

	//private long dontSendBefore = 0;

	public ReaderTask(DataResolutionManager dataSegmentator) {
		this.dataSegmentator = dataSegmentator;
	}

	@Override
	public void run() {

		try {

			ExpertPersistorManager.get().getUnprocessedSnapshots(filesProcessed);
			int all = filesProcessed.size();
			logger.debug("files processed in this round " + (all - last));
			dataSegmentator.prepareMultipleResolutionData();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
