package rcms.utilities.daqexpert.processing;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.DataManager;
import rcms.utilities.daqexpert.jobs.RecoveryJobManager;
import rcms.utilities.daqexpert.jobs.RecoveryRequest;
import rcms.utilities.daqexpert.jobs.RecoveryRequestBuilder;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.DominatingPersistor;
import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.persistence.Point;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;
import rcms.utilities.daqexpert.reasoning.causality.DominatingSelector;
import rcms.utilities.daqexpert.reasoning.processing.SnapshotProcessor;
import rcms.utilities.daqexpert.servlets.DummyDAQ;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Job processing the retrieved data (snapshots)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ProcessJob implements Callable<Triple<Set<Condition>, List<Point>, Condition>> {
	private final static StructureSerializer structureSerializer = new StructureSerializer();
	private final SnapshotProcessor snapshotProcessor;
	private final static Logger logger = Logger.getLogger(ProcessJob.class);

	private final DataManager dataManager;
	private final RecoveryJobManager recoveryJobManager;

	private final int priority;
	private final boolean includeExperimental;
	private final List<File> entries;


	private DominatingSelector dominatingSelector;


	private DominatingPersistor dominatingPersister;

	private static Set<Condition> allActiveConditions = new HashSet<>();

	private static Condition lastDominating;




	public ProcessJob(int priority, List<File> entries, DataManager dataManager, SnapshotProcessor snapshotProcessor, RecoveryJobManager recoveryManager, DominatingSelector dominatingSelector, DominatingPersistor dominatingPersister) {
		this.priority = priority;
		this.entries = entries;
		this.dataManager = dataManager;
		this.snapshotProcessor = snapshotProcessor;
		this.recoveryJobManager = recoveryManager;

		this.dominatingSelector = dominatingSelector;
		this.dominatingPersister = dominatingPersister;

		if (dataManager == null) {
			includeExperimental = true;
		} else {
			includeExperimental = false;
		}
	}

	/**
	 *
	 * @return Triple containging:
	 *  - produced conditions this round (based on this snapshot)
	 *  - produced points
	 *  - dominating condition (zero or one containing the most critical/root problem)
	 * @throws Exception
	 */
	public Triple<Set<Condition>, List<Point>, Condition> call() throws Exception {

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
					daq = structureSerializer.deserialize(file.getAbsolutePath().toString());

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


						allActiveConditions.addAll(logicResults);
						allActiveConditions = allActiveConditions.stream().filter(c->c.getEnd() == null && c.isShow() && c.isProblematic() && !c.isHoldNotifications()).collect(Collectors.toSet());


						//logger.info(allActiveConditions.stream().map(c->c.getTitle()).sorted().collect(Collectors.toList()));

						Condition dominating = dominatingSelector.selectDominating(allActiveConditions.stream().filter(c->c.isMature()).collect(Collectors.toSet()));

						if(dominating != lastDominating){
							dominatingPersister.persistDominating(lastDominating, dominating, new Date(daq.getLastUpdate()));
						}
						lastDominating = dominating;


						Long endProcessing = System.currentTimeMillis();
						processingTime += (endProcessing - startProcessing);

						result.addAll(logicResults);
					} else {
						logger.error("Snapshot not deserialized " + file.getAbsolutePath());
					}
				} catch (Exception e) {
					logger.error("Snapshot not deserialized: ", e);

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
			logger.debug(entries.size() + " files processed this round in " + time + "ms, " + "Deserialization time: "
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

		return Triple.of(result, points, lastDominating);
	}


	public int getPriority() {
		return priority;
	}

	public static void flush(){
		allActiveConditions = new HashSet<>();
	}
}