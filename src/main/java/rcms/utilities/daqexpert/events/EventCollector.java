package rcms.utilities.daqexpert.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class EventCollector implements EventRegister {

	private static final Logger logger = Logger.getLogger(EventCollector.class);

	private final List<Event> events = new ArrayList<>();

	@Override
	public void registerBegin(LogicModuleRegistry logicModule, Condition condition) {
		if (condition.isShow())
			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.debug("+ " + logicModule);

				Event event = new Event();
				event.setCondition(condition);
				event.setDate(condition.getStart());
				event.setType(EventType.ConditionStart);

				events.add(event);
			}
	}

	@Override
	public void registerEnd(LogicModuleRegistry logicModule, Condition condition) {
		if (condition.isShow())
			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.debug("- " + logicModule);

				Event event = new Event();
				event.setCondition(condition);
				event.setDate(condition.getEnd());
				event.setType(EventType.ConditionEnd);

				events.add(event);
			}

	}

	@Override
	public void registerUpdate(LogicModuleRegistry logicModule, Condition condition) {
		if (condition.isShow())

			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.debug("| " + logicModule);
			}

	}

	public List<Event> getEvents() {
		return events;
	}

}
