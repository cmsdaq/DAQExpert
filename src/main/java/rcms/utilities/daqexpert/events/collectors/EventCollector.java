package rcms.utilities.daqexpert.events.collectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.EventType;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;

public class EventCollector extends FilterRegister {

	private static final Logger logger = Logger.getLogger(EventCollector.class);

	private final List<ConditionEvent> events = new ArrayList<>();

	@Override
	public void registerBegin(Condition condition) {
		LogicModuleRegistry logicModule = condition.getLogicModule();

		boolean generate = isImportant(condition);

		if (generate) {

			ConditionEvent event = new ConditionEvent();
			event.setCondition(condition);
			event.setPriority(condition.getPriority());
			event.setDate(condition.getStart());
			event.setLogicModule(logicModule);
			events.add(event);

			if (logicModule.getLogicModule() instanceof SimpleLogicModule) {
				logger.debug("+ " + logicModule);
				event.setTitle("Start " + condition.getTitle());
				event.setType(EventType.ConditionStart);

			} else if (logicModule.getLogicModule() instanceof ComparatorLogicModule) {
				logger.debug("# " + logicModule);
				event.setTitle(logicModule.getDescription() + ": " + condition.getTitle());
				event.setType(EventType.Single);
			}
		}

	}

	@Override
	public void registerEnd(Condition condition) {

		LogicModuleRegistry logicModule = condition.getLogicModule();
		boolean generate = isImportant(condition);

		if (generate) {
			

			if (logicModule.getLogicModule() instanceof SimpleLogicModule) {
				logger.debug("- " + logicModule);
				ConditionEvent event = new ConditionEvent();
				event.setPriority(condition.getPriority());
				event.setCondition(condition);
				event.setDate(condition.getEnd());
				event.setType(EventType.ConditionEnd);
				event.setLogicModule(logicModule);
				events.add(event);
				event.setTitle("End " + condition.getTitle());
			} else if (logicModule.getLogicModule() instanceof ComparatorLogicModule) {
				// nothing to do here - send notification on start of comparator
				// LM
			}

		}
	}

	@Override
	public void registerUpdate(Condition condition) {

		LogicModuleRegistry logicModule = condition.getLogicModule();
		boolean generate = isImportant(condition);

		if (generate) {

			logger.debug("| " + logicModule);
		}

	}

	public List<ConditionEvent> getEvents() {
		return events;
	}

}
