package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.segmentation.DataResolution;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

/**
 * Job processing the retrieved data (snapshots)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ProcessJob implements Callable<Set<Entry>> {
	private final static StructureSerializer structureSerializer = new StructureSerializer();
	private final SnapshotProcessor snapshotProcessor;
	private final static Logger logger = Logger.getLogger(ProcessJob.class);

	private final DataManager dataManager;

	private final int priority;
	private final boolean includeExperimental;
	private final List<File> entries;

	public ProcessJob(int priority, List<File> entries, DataManager dataManager, SnapshotProcessor snapshotProcessor) {
		this.priority = priority;
		this.entries = entries;
		this.dataManager = dataManager;
		this.snapshotProcessor = snapshotProcessor;
		if (dataManager == null) {
			includeExperimental = true;
		} else {
			includeExperimental = false;
		}
	}

	public Set<Entry> call() throws Exception {

		Set<Entry> result = new LinkedHashSet<>();

		DAQ daq = null;
		for (File file : entries) {

			daq = structureSerializer.deserialize(file.getAbsolutePath().toString(), PersistenceFormat.SMILE);

			if (daq != null) {

				if (dataManager != null) {
					// this is not done in on-demand requests
					dataManager.addSnapshot(new DummyDAQ(daq));
				}
				Set<Entry> logicResults = snapshotProcessor.process(daq, true, includeExperimental);
				result.addAll(logicResults);
			} else {
				logger.error("Snapshot not deserialized " + file.getAbsolutePath());
			}

		}

		logger.debug("files processed in this round " + entries.size());
		logger.trace("values in data manager " + Application.get().getDataManager().getRawDataByResolution()
				.get(DataResolution.Full).get(DataStream.EVENTS));

		if (daq != null) {
			logger.info("Temporarly finishing events");
			Set<Entry> finished = snapshotProcessor.getEventProducer().finish(new Date(daq.getLastUpdate()));
			// Application.get().getDataManager().getResult().addAll(finished);
			logger.debug("Finishing the round.");
			result.addAll(finished);
		}

		return result;
	}

	public int getPriority() {
		return priority;
	}
}