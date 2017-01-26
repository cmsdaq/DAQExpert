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

		Long start = System.currentTimeMillis();

		int deserializingTime = 0;
		int processingTime = 0;
		int segmentingTime = 0;

		Set<Entry> result = new LinkedHashSet<>();

		DAQ daq = null;

		if (includeExperimental) {
			logger.info("Experimental logic modules job");
			snapshotProcessor.getCheckManager().getExperimentalProcessor().loadExperimentalLogicModules();
		}

		for (File file : entries) {

			try {

				Long startDeserializing = System.currentTimeMillis();
				daq = structureSerializer.deserialize(file.getAbsolutePath().toString(), PersistenceFormat.SMILE);
				Long endDeserializing = System.currentTimeMillis();
				deserializingTime += (endDeserializing - startDeserializing);

				if (daq != null) {

					Long startSegmenting = System.currentTimeMillis();
					if (dataManager != null) {
						// this is not done in on-demand requests
						dataManager.addSnapshot(new DummyDAQ(daq));
						dataManager.setLastUpdate(new Date(daq.getLastUpdate()));
					}
					Long endSegmenting = System.currentTimeMillis();
					segmentingTime += (endSegmenting - startSegmenting);

					Long startProcessing = System.currentTimeMillis();
					Set<Entry> logicResults = snapshotProcessor.process(daq, true, includeExperimental);
					Long endProcessing = System.currentTimeMillis();
					processingTime += (endProcessing - startProcessing);

					result.addAll(logicResults);
				} else {
					logger.error("Snapshot not deserialized " + file.getAbsolutePath());
				}

			} catch (RuntimeException e) {
				logger.error("Error processing files " + file);
			}

		}

		Long end = System.currentTimeMillis();
		int time = (int) (end - start);

		if (entries.size() > 0)
			logger.info(entries.size() + " files processed this round in " + time + "ms, " + "Deserialization time: "
					+ deserializingTime + ", segmenting time: " + segmentingTime + ", processing time: "
					+ processingTime);
		logger.trace("values in data manager " + Application.get().getDataManager().getRawDataByResolution()
				.get(DataResolution.Full).get(DataStream.EVENTS));

		if (daq != null) {
			logger.debug("Temporarly finishing events");
			Set<Entry> finished = snapshotProcessor.getEventProducer().finish(new Date(daq.getLastUpdate()));
			// Application.get().getDataManager().getResult().addAll(finished);
			logger.debug("Force finishing returned with results: " + finished);
			result.addAll(finished);
		}

		return result;
	}

	public int getPriority() {
		return priority;
	}
}