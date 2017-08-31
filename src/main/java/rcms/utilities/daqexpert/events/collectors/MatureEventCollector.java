package rcms.utilities.daqexpert.events.collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.EventType;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;

public class MatureEventCollector extends FilterRegister {

	private static final Logger logger = Logger.getLogger(MatureEventCollector.class);

	private final List<ConditionEvent> events = new ArrayList<>();

	private final List<Condition> immature = new ArrayList<>();

	private ConditionEvent generateConditionStartEvent(Condition condition) {

		ConditionEvent event = new ConditionEvent();
		event.setTitle("Started: " + condition.getTitle());
		event.setCondition(condition);
		event.setPriority(condition.getPriority());
		event.setDate(condition.getStart());
		event.setType(EventType.ConditionStart);
		event.setLogicModule(condition.getLogicModule());
		return event;
	}

	@Override
	public void registerBegin(Condition condition) {
		LogicModuleRegistry logicModule = condition.getLogicModule();

		boolean generate = isImportant(condition);

		if (generate) {
			if (condition.isMature()) {
				ConditionEvent event = new ConditionEvent();
				event.setCondition(condition);
				event.setPriority(condition.getPriority());
				event.setDate(condition.getStart());
				event.setLogicModule(logicModule);
				events.add(event);

				if (logicModule.getLogicModule() instanceof SimpleLogicModule) {

					logger.debug("+ " + logicModule);
					event.setTitle("Started: " + condition.getTitle());
					event.setType(EventType.ConditionStart);

				} else if (logicModule.getLogicModule() instanceof ComparatorLogicModule) {
					logger.debug("# " + logicModule);
					event.setTitle(logicModule.getDescription() + ": " + condition.getTitle());
					event.setType(EventType.Single);
				}
			} else {
				immature.add(condition);
			}

		}

	}

	@Override
	public void registerEnd(Condition condition) {

		LogicModuleRegistry logicModule = condition.getLogicModule();
		boolean generate = isImportant(condition);

		if (generate) {
			if (condition.isMature()) {

				if (logicModule.getLogicModule() instanceof SimpleLogicModule) {

					logger.debug("- " + logicModule);
					ConditionEvent event = new ConditionEvent();
					event.setPriority(condition.getPriority());
					event.setCondition(condition);
					event.setDate(condition.getEnd());
					event.setType(EventType.ConditionEnd);
					event.setLogicModule(logicModule);
					events.add(event);
					event.setTitle("Ended: " + condition.getTitle());

				} else if (logicModule.getLogicModule() instanceof ComparatorLogicModule) {
					// nothing to do here - send notification on start of
					// comparator
					// LM
				}
			} else {
				// nothing to do here - if condition ends and it's not
				// mature -
				// nothing will change
			}

		}
	}

	@Override
	public void registerUpdate(Condition condition) {

		LogicModuleRegistry logicModule = condition.getLogicModule();
		boolean generate = isImportant(condition);

		if (generate) {
			if (condition.isMature()) {

				logger.debug("| " + logicModule);
			}
		}

	}

	public List<ConditionEvent> getEvents() {
		return events;
	}

	public void verifyImmature() {

		int initial = immature.size();
		int removed = 0;
		List<Integer> toRemove = new ArrayList<>();
		for (int i = 0; i < immature.size(); i++) {
			Condition condition = immature.get(i);
			// for (Condition condition : immature) {
			if (condition.getEnd() != null) {
				/* Condition finished but never become mature */
				// immature.remove(condition);
				toRemove.add(i);
			} else if (condition.isMature()) {
				/* Condition matured */
				events.add(generateConditionStartEvent(condition));
				// immature.remove(condition);
				toRemove.add(i);
				removed++;
			}
		}
		Collections.reverse(toRemove);
		for (int idx : toRemove) {
			Condition removedElement = immature.remove(idx);
			if (removedElement == null) {
				logger.warn("Could not remove the element");
			}
		}
		if (initial != 0 && removed != 0) {
			logger.debug("Some conditiones matured, before there were " + initial + " immature events, matured: "
					+ removed + " this round, now: " + immature.size());
		}
	}

}
