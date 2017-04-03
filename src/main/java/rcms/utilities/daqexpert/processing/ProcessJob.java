package rcms.utilities.daqexpert.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

/**
 * Job processing the retrieved data (snapshots)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ProcessJob implements Callable<Pair<Set<Condition>, List<Point>>> {
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

	public Pair<Set<Condition>, List<Point>> call() throws Exception {

		Long start = System.currentTimeMillis();

		int deserializingTime = 0;
		int processingTime = 0;
		int segmentingTime = 0;

		Set<Condition> result = new LinkedHashSet<>();
		List<Point> points = new ArrayList<>();

		DAQ daq = null;

		if (includeExperimental) {
			logger.info("Experimental logic modules job");
			snapshotProcessor.getCheckManager().getExperimentalProcessor().loadExperimentalLogicModules();
		}

		Long firstSnapshot = null;
		Long lastSnapshot = null;

		for (File file : entries) {

			try {

				try {
					Long startDeserializing = System.currentTimeMillis();
					daq = structureSerializer.deserialize(file.getAbsolutePath().toString(), PersistenceFormat.SMILE);

					Long endDeserializing = System.currentTimeMillis();
					deserializingTime += (endDeserializing - startDeserializing);

					if (daq != null) {

						if (firstSnapshot == null) {
							firstSnapshot = daq.getLastUpdate();
						}

						Long startSegmenting = System.currentTimeMillis();
						if (dataManager != null) {
							// this is not done in on-demand requests
							points.addAll(dataManager.addSnapshot(new DummyDAQ(daq)));
							dataManager.setLastUpdate(new Date(daq.getLastUpdate()));
						}
						Long endSegmenting = System.currentTimeMillis();
						segmentingTime += (endSegmenting - startSegmenting);

						Long startProcessing = System.currentTimeMillis();
						Set<Condition> logicResults = snapshotProcessor.process(daq, includeExperimental);
						Long endProcessing = System.currentTimeMillis();
						processingTime += (endProcessing - startProcessing);

						result.addAll(logicResults);
					} else {
						logger.error("Snapshot not deserialized " + file.getAbsolutePath());
					}
				} catch (Exception e) {
					logger.error("Snapshot not desierialized: " + e);

				}

			} catch (RuntimeException e) {
				logger.error("Error processing files " + file);
			}

		}

		if (daq == null) {
			logger.info("This round there was only one snapshot and there was problem with it, aborting..");
			return null;
		}

		lastSnapshot = daq.getLastUpdate();

		Long end = System.currentTimeMillis();
		int time = (int) (end - start);

		if (entries.size() > 0) {
			logger.info(entries.size() + " files processed this round in " + time + "ms, " + "Deserialization time: "
					+ deserializingTime + ", segmenting time: " + segmentingTime + ", processing time: "
					+ processingTime);
			logger.debug("Snapshots processed: " + new Date(firstSnapshot) + " - " + new Date(lastSnapshot));
		}

		if (daq != null) {
			/*
			 * logger.debug("Temporarly finishing events"); Set<Condition>
			 * finished = snapshotProcessor.getEventProducer().finish(new
			 * Date(daq.getLastUpdate())); //
			 * Application.get().getDataManager().getResult().addAll(finished);
			 * logger.debug("Force finishing returned with results: " +
			 * finished); result.addAll(finished);
			 */
		}

		return Pair.of(result, points);
	}

	public int getPriority() {
		return priority;
	}
}