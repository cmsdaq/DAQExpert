package rcms.utilities.daqexpert.events;

import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class EventPrinter implements EventRegister {

	private static final Logger logger = Logger.getLogger(EventPrinter.class);

	@Override
	public void registerBegin(LogicModuleRegistry logicModule, Condition condition) {
		if (condition.isShow())
			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.info("+ " + logicModule);
			}
	}

	@Override
	public void registerEnd(LogicModuleRegistry logicModule, Condition condition) {
		if (condition.isShow())
			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.info("- " + logicModule);
			}

	}

	@Override
	public void registerUpdate(LogicModuleRegistry logicModule, Condition condition) {
		if (condition.isShow())

			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.info("| " + logicModule);
			}

	}

	@Override
	public List<ConditionEvent> getEvents() {
		return null;
	}

}
