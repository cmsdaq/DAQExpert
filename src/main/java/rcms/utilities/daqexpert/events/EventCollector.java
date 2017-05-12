package rcms.utilities.daqexpert.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class EventCollector implements EventRegister {

	private static final Logger logger = Logger.getLogger(EventCollector.class);

	private final List<ConditionEvent> events = new ArrayList<>();

	@Override
	public void registerBegin(Condition condition) {
		LogicModuleRegistry logicModule = condition.getLogicModule();
		if (logicModule == null) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Condition has no logic module assigned");
		}
		if (condition.isShow()) {
			if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()
					|| logicModule.getLogicModule() instanceof ContextLogicModule) {
				logger.debug("+ " + logicModule);

				ConditionEvent event = new ConditionEvent();
				event.setTitle("Start " + condition.getTitle());
				event.setCondition(condition);
				event.setPriority(condition.getPriority());
				event.setDate(condition.getStart());
				event.setType(EventType.ConditionStart);
				event.setLogicModule(logicModule);

				events.add(event);
			}
			if (logicModule.getLogicModule() instanceof ComparatorLogicModule) {
				logger.debug("# " + logicModule);

				ConditionEvent event = new ConditionEvent();
				event.setTitle(logicModule.getDescription() + ": " + condition.getTitle());
				event.setCondition(condition);
				event.setPriority(condition.getPriority());
				event.setDate(condition.getStart());
				event.setType(EventType.Single);
				event.setLogicModule(logicModule);

				events.add(event);
			}
		}
	}

	@Override
	public void registerEnd(Condition condition) {
		LogicModuleRegistry logicModule = condition.getLogicModule();
		if (logicModule == null) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Condition has no logic module assigned");
		}
		if (condition.isShow())
			if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()
					|| logicModule.getLogicModule() instanceof ContextLogicModule) {
				logger.debug("- " + logicModule);

				ConditionEvent event = new ConditionEvent();
				event.setTitle("End " + condition.getTitle());
				event.setPriority(condition.getPriority());
				event.setCondition(condition);
				event.setDate(condition.getEnd());
				event.setType(EventType.ConditionEnd);
				event.setLogicModule(logicModule);

				events.add(event);
			}

	}

	@Override
	public void registerUpdate(Condition condition) {
		LogicModuleRegistry logicModule = condition.getLogicModule();
		if (logicModule == null) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Condition has no logic module assigned");
		}
		if (condition.isShow())

			if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()
					|| logicModule.getLogicModule() instanceof ContextLogicModule) {
				logger.debug("| " + logicModule);
			}

	}

	public List<ConditionEvent> getEvents() {
		return events;
	}

}
