package rcms.utilities.daqaggregator.reasoning.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class EventProducer {

	/** Logger */
	private static final Logger logger = Logger.getLogger(EventProducer.class);

	/** Singleton instance */
	private static EventProducer instance;

	/** All produced reasons are kept in this list */
	private final List<Entry> result =  Collections.synchronizedList(new ArrayList<Entry>());

	/** All events without end date are kept here (unfinished) */
	private final Map<String, Entry> unfinished = new HashMap<>();

	/** Current states are kept here */
	private final Map<String, Boolean> states = new HashMap<>();

	/** Singleton constructor */
	private EventProducer() {
	}

	/** Get singleton instance */
	public static EventProducer get() {
		if (instance == null)
			instance = new EventProducer();
		return instance;
	}

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
		return produce(checker, value, date, checker.getLevel());
	}

	/**
	 * 00000100000100000100 will produce 3 events corresponding to 1 start and
	 * ending on next 1 start
	 */
	public void produce(Comparator comparator, boolean value, Date last, Date current) {

		if (value) {
			logger.debug("New lazy event " + current);
			produce(comparator, !value, current, comparator.getLevel());
			produce(comparator, value, current, comparator.getLevel());
		}
	}

	private Entry produce(Classificable classificable, boolean value, Date date, Level level) {
		// get current state
		String className = classificable.getClass().getSimpleName();
		String content = classificable.getText();
		EventClass eventClass = classificable.getClassName();
		Entry result = null;
		if (states.containsKey(className)) {
			boolean currentState = states.get(className);

			if (currentState != value) {
				result = finishOldAddNew(className, content, value, date, level, eventClass);
				states.put(className, value);
			} else {
				result = unfinished.get(className);
			}
		}

		// no prior states
		else {
			states.put(className, value);
			result = finishOldAddNew(className, content, value, date, level, eventClass);
		}
		return result;
	}

	private Entry finishOldAddNew(String className, String content, Boolean value, Date date, Level level,
			EventClass eventClass) {

		/* finish old entry */
		if (unfinished.containsKey(className)) {
			Entry toFinish = unfinished.get(className);
			toFinish.setEnd(date);
			toFinish.calculateDuration();
		}

		/* add new entry */
		Entry entry = new Entry();
		entry.setClassName(eventClass.getCode());
		entry.setContent(content);
		entry.setShow(value);
		entry.setStart(date);
		entry.setGroup(level.getCode());

		result.add(entry);
		unfinished.put(className, entry);
		return entry;
	}

	/**
	 * Get all results produced by event producer
	 * 
	 * @return list of events produced
	 */
	public List<Entry> getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "EventProducer [result=" + result + ", states=" + states + ", unfinished=" + unfinished + "]";
	}

}
