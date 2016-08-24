package rcms.utilities.daqexpert;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.reasoning.base.EventProducer;
import rcms.utilities.daqexpert.reasoning.base.SnapshotProcessor;

/**
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ReaderTask implements Runnable {

	private static final Logger logger = Logger.getLogger(ReaderTask.class);



	protected DataResolutionManager dataSegmentator;
	protected SnapshotProcessor snapshotProcessor;
	protected EventProducer eventProducer;

	public Future future;

	public ReaderTask(DataResolutionManager dataSegmentator) {
		this.dataSegmentator = dataSegmentator;
		this.eventProducer = new EventProducer();
		this.snapshotProcessor = new SnapshotProcessor(eventProducer);
	}


}
