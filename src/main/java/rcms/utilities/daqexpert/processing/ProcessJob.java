package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.reasoning.processing.EventProducer;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

/**
 * Job processing the retrieved data (snapshots)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ProcessJob implements Callable<Long> {
	private final static StructureSerializer structureSerializer = new StructureSerializer();
	private final static EventProducer eventProducer = new EventProducer();
	private final static SnapshotProcessor snapshotProcessor = new SnapshotProcessor(eventProducer);
	private final static Logger logger = Logger.getLogger(ProcessJob.class);

	private final int priority;
	private final List<File> entries;

	public ProcessJob(int priority, List<File> entries) {
		this.priority = priority;
		this.entries = entries;
	}

	public Long call() throws Exception {

		DAQ daq = null;
		for (File file : entries) {

			daq = structureSerializer.deserialize(file.getAbsolutePath().toString(), PersistenceFormat.SMILE);

			if (daq != null) {
				Application.get().getDataManager().addSnapshot(new DummyDAQ(daq));
				snapshotProcessor.process(daq, true);
			} else {
				logger.error("Snapshot not deserialized " + file.getAbsolutePath());
			}

		}

		logger.debug("files processed in this round " + entries.size());
		logger.trace("values in data manager " + Application.get().getDataManager().getRawDataByResolution()
				.get(DataResolution.Full).get(DataStream.EVENTS));

		if (daq != null) {
			eventProducer.finish(new Date(daq.getLastUpdate()));
			logger.debug("Finishing the round.");
		}

		return 0L;
	}

	public int getPriority() {
		return priority;
	}
}