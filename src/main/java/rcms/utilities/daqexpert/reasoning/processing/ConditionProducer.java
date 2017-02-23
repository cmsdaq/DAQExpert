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

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Context;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * From checker & comparator boolean results creates events
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ConditionProducer {

	public ConditionProducer() {
		unfinished = new HashMap<>();
		states = new HashMap<>();
		finishedThisRound = new ArrayList<>();
	}

	/** Logger */
	private static final Logger logger = Logger.getLogger(ConditionProducer.class);

	/** All events without end date are kept here (unfinished) */
	private final Map<String, Condition> unfinished;

	/** Current states are kept here */
	private final Map<String, Boolean> states;

	private final List<Condition> finishedThisRound;

	/**
	 * Get all unfinished reasons and force finish them (so can be displayed)
	 * 
	 * @param date
	 *            date on which unfinished reasons will be finished
	 */
	public Set<Condition> finish(Date date) {

		logger.debug("Artificial finishing with unfinished events: " + unfinished);
		logger.trace("finishedTR: " + finishedThisRound);

		Set<Condition> result = new HashSet<>();

		for (Condition entry : unfinished.values()) {
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
	public Pair<Boolean, Condition> produce(SimpleLogicModule checker, boolean value, Date date) {
		return build(checker, value, date);
	}

	/**
	 * 00000100000100000100 will produce 3 events corresponding to 1 start and
	 * ending on next 1 start
	 */
	public Pair<Boolean, Condition> produce(ComparatorLogicModule comparator, boolean value, Date last, Date current) {

		if (value) {
			logger.debug("New lazy event " + current);
			build(comparator, !value, current);
			Pair<Boolean, Condition> b = build(comparator, value, current);
			b.getRight().setShow(true);

			logger.trace("Result for comparator LM: " + b.getLeft());
			return b;
		}

		return Pair.of(false, null);

	}

	private Pair<Boolean, Condition> build(LogicModule logicModule, boolean value, Date date) {
		// get current state
		String logicModuleName = logicModule.getClass().getSimpleName();
		String content = logicModule.getName();
		ConditionPriority eventClass = logicModule.getPriority();

		Context context = null;

		if (logicModule instanceof ActionLogicModule) {
			context = ((ActionLogicModule) logicModule).getContext();
		}

		Boolean leftResult = false;
		Condition result = null;
		if (states.containsKey(logicModuleName)) {
			boolean currentState = states.get(logicModuleName);

			if (currentState != value) {
				result = finishOldAddNew(logicModule, content, value, date, eventClass, context);
				leftResult = true;
				states.put(logicModuleName, value);
			} else {
				result = unfinished.get(logicModuleName);
			}
		}

		// no prior states
		else {
			states.put(logicModuleName, value);
			result = finishOldAddNew(logicModule, content, value, date, eventClass, context);
			leftResult = true;
		}
		result.setLogicModule(logicModule.getLogicModuleRegistry());
		if (logicModule.getLogicModuleRegistry() != null) {
			result.setGroup(logicModule.getLogicModuleRegistry().getGroup());
		}else{
			result.setGroup(ConditionGroup.HIDDEN);
		}

		if (value) {
			if (logicModule instanceof ActionLogicModule) {
				ActionLogicModule alm = (ActionLogicModule) logicModule;
				logger.debug("Putting message into context: " + logicModule.getDescription());
				if (result.getDescription() == null) {
					result.setDescription(alm.getContext().getMessageWithContext(logicModule.getDescription()));
				}
				if (result.getActionSteps() == null) {
					result.setActionSteps(
							alm.getContext().getActionWithContext(((ActionLogicModule) logicModule).getAction()));
				}

			} else {
				result.setDescription(logicModule.getDescription());

			}
		}
		return Pair.of(leftResult, result);
	}

	protected Condition finishOldAddNew(LogicModule logicModule, String content, Boolean value, Date date,
			ConditionPriority eventClass, Context context) {

		String logicModuleName = logicModule.getClass().getSimpleName();

		/* finish old entry */
		if (unfinished.containsKey(logicModuleName)) {
			Condition toFinish = unfinished.get(logicModuleName);
			toFinish.setState(EntryState.FINISHED);
			toFinish.setEnd(date);
			toFinish.calculateDuration();
			Context clone = (Context) org.apache.commons.lang.SerializationUtils.clone(context);
			toFinish.setFinishedContext(clone);
			if (!toFinish.getStart().equals(toFinish.getEnd())) {
				logger.debug("Finishing entry " + toFinish.getTitle() + " with id: " + toFinish.getId());
				finishedThisRound.add(toFinish);
			}
		}

		/* add new condition */
		Condition condition = new Condition();
		condition.setClassName(eventClass);
		condition.setTitle(content);
		condition.setShow(value);
		condition.setStart(date);

		unfinished.put(logicModuleName, condition);
		return condition;
	}

	@Override
	public String toString() {
		return "EventProducer [states=" + states + ", unfinished=" + unfinished + "]";
	}

	public List<Condition> getFinishedThisRound() {
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

	protected Map<String, Condition> getUnfinished() {
		return unfinished;
	}

}
