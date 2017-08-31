package rcms.utilities.daqexpert.reasoning.processing;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.events.collectors.MatureEventCollector;
import rcms.utilities.daqexpert.persistence.Condition;

/**
 * Processes snapshot in analysis
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SnapshotProcessor {

	private final LogicModuleManager checkManager;

	private static final Logger logger = Logger.getLogger(SnapshotProcessor.class);

	private ConditionProducer eventProducer;

	public SnapshotProcessor(ConditionProducer eventProducer) {

		this.eventProducer = eventProducer;
		this.checkManager = new LogicModuleManager(eventProducer);
	}

	public Set<Condition> process(DAQ daqSnapshot, boolean includeExperimental) {
		logger.trace("Process snapshot " + new Date(daqSnapshot.getLastUpdate()));
		Set<Condition> result = new LinkedHashSet<>();
		try {
			List<Condition> lmResults = checkManager.runLogicModules(daqSnapshot, includeExperimental);

			for (Condition lmResult : lmResults) {
				result.add(lmResult);
			}

			if(eventProducer.getEventRegister() instanceof MatureEventCollector){
				MatureEventCollector mec = (MatureEventCollector) eventProducer.getEventRegister();
				mec.verifyImmature();
			}
			// Application.get().getDataManager().getResult().addAll(result);

			logger.debug("Results from CheckManager for this snapshot: " + lmResults);

		} catch (RuntimeException e) {
			logger.error("Exception processing snapshot", e);
		}
		return result;
	}

	public ConditionProducer getEventProducer() {
		return eventProducer;
	}

	public void clearProducer() {
		eventProducer.clearProducer();// = new EventProducer();
	}

	public LogicModuleManager getCheckManager() {
		return checkManager;
	}

}
