package rcms.utilities.daqexpert.events;

import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class EventPrinter implements EventRegister {

	private static final Logger logger = Logger.getLogger(EventPrinter.class);

	@Override
	public void registerBegin(Condition condition) {
		if (condition.isShow())
			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.info("+ " + condition.getLogicModule());
			}
	}

	@Override
	public void registerEnd(Condition condition) {
		if (condition.isShow())
			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.info("- " + condition.getLogicModule());
			}

	}

	@Override
	public void registerUpdate(Condition condition) {
		if (condition.isShow())

			if (condition.getPriority() == ConditionPriority.CRITICAL) {
				logger.info("| " + condition.getLogicModule());
			}

	}

	@Override
	public List<ConditionEvent> getEvents() {
		return null;
	}

}
