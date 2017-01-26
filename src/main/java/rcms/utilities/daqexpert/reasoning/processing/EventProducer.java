package rcms.utilities.daqexpert.reasoning.processing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.PersistenceManager;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

/**
 * From checker & comparator boolean results creates events
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class EventProducer {

	public EventProducer(PersistenceManager persistenceManager) {
		unfinished = new HashMap<>();
		states = new HashMap<>();
		finishedThisRound = new ArrayList<>();
		this.persistenceManager = persistenceManager;
	}

	private final PersistenceManager persistenceManager;

	/** Logger */
	private static final Logger logger = Logger.getLogger(EventProducer.class);

	/** All events without end date are kept here (unfinished) */
	private final Map<String, Entry> unfinished;

	/** Current states are kept here */
	private final Map<String, Boolean> states;

	private final List<Entry> finishedThisRound;

	/**
	 * Get all unfinished reasons and force finish them (so can be displayed)
	 * 
	 * @param date
	 *            date on which unfinished reasons will be finished
	 */
	public Set<Entry> finish(Date date) {

		logger.debug("Artificial finishing with unfinished events: " + unfinished);
		logger.trace("finishedTR: " + finishedThisRound);

		Set<Entry> result = new HashSet<>();

		for (Entry entry : unfinished.values()) {
			entry.setEnd(date);
			entry.calculateDuration();

			if (entry.isShow()) {
				result.add(entry);
			}

		}
		return result;
	}

	/**
	 * Produces events for value 111000111000 will produce 2 events
	 * corresponding to 1 start and end time
	 */
	public Pair<Boolean, Entry> produce(SimpleLogicModule checker, boolean value, Date date) {
		return produce(checker, value, date, checker.getGroup());
	}

	/**
	 * 00000100000100000100 will produce 3 events corresponding to 1 start and
	 * ending on next 1 start
	 */
	public Pair<Boolean, Entry> produce(ComparatorLogicModule comparator, boolean value, Date last, Date current) {

		if (value) {
			logger.debug("New lazy event " + current);
			produce(comparator, !value, current, comparator.getGroup());
			Pair<Boolean, Entry> b = produce(comparator, value, current, comparator.getGroup());
			b.getRight().setShow(true);

			logger.trace("Result for comparator LM: " + b.getLeft());
			return b;
		}

		return Pair.of(false, null);

	}

	private Pair<Boolean, Entry> produce(LogicModule classificable, boolean value, Date date, EventGroup level) {
		// get current state
		String className = classificable.getClass().getSimpleName();
		String content = classificable.getName();
		EventPriority eventClass = classificable.getPriority();

		Context context = null;

		if (classificable instanceof ActionLogicModule) {
			context = ((ActionLogicModule) classificable).getContext();
		}

		Boolean leftResult = false;
		Entry result = null;
		if (states.containsKey(className)) {
			boolean currentState = states.get(className);

			if (currentState != value) {
				result = finishOldAddNew(className, content, value, date, level, eventClass, context);
				leftResult = true;
				states.put(className, value);
			} else {
				result = unfinished.get(className);
			}
		}

		// no prior states
		else {
			states.put(className, value);
			result = finishOldAddNew(className, content, value, date, level, eventClass, context);
			leftResult = true;
		}
		result.setEventFinder(classificable);
		return Pair.of(leftResult, result);
	}

	protected Entry finishOldAddNew(String className, String content, Boolean value, Date date, EventGroup level,
			EventPriority eventClass, Context context) {

		/* finish old entry */
		if (unfinished.containsKey(className)) {
			Entry toFinish = unfinished.get(className);
			toFinish.setState(EntryState.FINISHED);
			toFinish.setEnd(date);
			toFinish.calculateDuration();
			Context clone = (Context) org.apache.commons.lang.SerializationUtils.clone(context);
			toFinish.setFinishedContext(clone);
			if (!toFinish.getStart().equals(toFinish.getEnd()) && toFinish.getId() != null){
				logger.debug("Finishing entry " + toFinish.getContent() + " with id: " + toFinish.getId() );
				finishedThisRound.add(toFinish);
				persistenceManager.persist(toFinish);
			}
		}

		/* add new entry */
		Entry entry = new Entry();
		entry.setClassName(eventClass.getCode());
		entry.setContent(content);
		entry.setShow(value);
		entry.setStart(date);
		entry.setGroup(level.getCode());
		if (entry.isShow()){
			persistenceManager.persist(entry);
			logger.debug("Persisted entry: " + entry.getContent() + " with id: " + entry.getId());
		}

		// result.add(entry);

		// Application.get().getDataManager().getResult().add(entry);
		unfinished.put(className, entry);
		return entry;
	}

	@Override
	public String toString() {
		return "EventProducer [states=" + states + ", unfinished=" + unfinished + "]";
	}

	public List<Entry> getFinishedThisRound() {
		return finishedThisRound;
	}

	public void clearFinishedThisRound() {
		finishedThisRound.clear();
	}

	public void clearProducer() {
		logger.info("Clearing producer");
		for (java.util.Map.Entry<String, Boolean> state : states.entrySet()) {
			state.setValue(false);
		}
		states.clear();
		unfinished.clear();
		finishedThisRound.clear();
	}

	protected Map<String, Entry> getUnfinished() {
		return unfinished;
	}

}
