package rcms.utilities.daqexpert.reasoning.processing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.reasoning.base.Comparator;
import rcms.utilities.daqexpert.reasoning.base.Condition;
import rcms.utilities.daqexpert.reasoning.base.ContextCollector;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.EventFinder;
import rcms.utilities.daqexpert.reasoning.base.ExtendedCondition;
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

	/** Logger */
	private static final Logger logger = Logger.getLogger(EventProducer.class);


	/** All events without end date are kept here (unfinished) */
	private final Map<String, Entry> unfinished = new HashMap<>();

	/** Current states are kept here */
	private final Map<String, Boolean> states = new HashMap<>();

	private final List<Entry> finishedThisRound = new ArrayList<>();


	/**
	 * Get all unfinished reasons and force finish them (so can be displayed)
	 * 
	 * @param date
	 *            date on which unfinished reasons will be finished
	 */
	public void finish(Date date) {
		for (Entry entry : unfinished.values()) {
			entry.setEnd(date);
			entry.calculateDuration();
			// entry.setEnd(null);
		}
	}

	/**
	 * Produces events for value 111000111000 will produce 2 events
	 * corresponding to 1 start and end time
	 */
	public Entry produce(Condition checker, boolean value, Date date) {
		return produce(checker, value, date, checker.getGroup());
	}

	/**
	 * 00000100000100000100 will produce 3 events corresponding to 1 start and
	 * ending on next 1 start
	 */
	public void produce(Comparator comparator, boolean value, Date last, Date current) {

		if (value) {
			logger.debug("New lazy event " + current);
			produce(comparator, !value, current, comparator.getGroup());
			produce(comparator, value, current, comparator.getGroup());
		}
	}

	private Entry produce(EventFinder classificable, boolean value, Date date, EventGroup level) {
		// get current state
		String className = classificable.getClass().getSimpleName();
		String content = classificable.getName();
		EventPriority eventClass = classificable.getPriority();

		ContextCollector context = null;

		if (classificable instanceof ExtendedCondition) {
			context = ((ExtendedCondition) classificable).getContext();
		}

		Entry result = null;
		if (states.containsKey(className)) {
			boolean currentState = states.get(className);

			if (currentState != value) {
				result = finishOldAddNew(className, content, value, date, level, eventClass, context);
				states.put(className, value);
			} else {
				result = unfinished.get(className);
			}
		}

		// no prior states
		else {
			states.put(className, value);
			result = finishOldAddNew(className, content, value, date, level, eventClass, context);
		}
		result.setEventFinder(classificable);
		return result;
	}

	private Entry finishOldAddNew(String className, String content, Boolean value, Date date, EventGroup level,
			EventPriority eventClass, ContextCollector context) {

		/* finish old entry */
		if (unfinished.containsKey(className)) {
			Entry toFinish = unfinished.get(className);
			toFinish.setState(EntryState.FINISHED);
			toFinish.setEnd(date);
			toFinish.calculateDuration();
			ContextCollector clone = (ContextCollector) org.apache.commons.lang.SerializationUtils.clone(context);
			toFinish.setFinishedContext(clone);
			finishedThisRound.add(toFinish);
		}

		/* add new entry */
		Entry entry = new Entry();
		entry.setClassName(eventClass.getCode());
		entry.setContent(content);
		entry.setShow(value);
		entry.setStart(date);
		entry.setGroup(level.getCode());

		//result.add(entry);

		Application.get().getDataManager().getResult().add(entry);
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
	
	public void clearProducer(){
		states.clear();
		unfinished.clear();
		finishedThisRound.clear();
	}

}
