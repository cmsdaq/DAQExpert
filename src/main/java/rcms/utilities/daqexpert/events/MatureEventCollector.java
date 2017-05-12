package rcms.utilities.daqexpert.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public class MatureEventCollector implements EventRegister, Observer {

	private static final Logger logger = Logger.getLogger(MatureEventCollector.class);

	private final List<ConditionEvent> events = new ArrayList<>();

	private final Set<Condition> immature = new HashSet<>();

	private ConditionEvent generateConditionStartEvent(Condition condition) {

		ConditionEvent event = new ConditionEvent();
		event.setTitle("Start " + condition.getTitle());
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
		if (logicModule == null) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Condition has no logic module assigned");
		}
		if (condition.isShow()) {
			if (logicModule.getLogicModule() instanceof SimpleLogicModule) {
				if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()) {
					if (condition.isMature()) {
						logger.debug("+ " + logicModule);
						ConditionEvent event = generateConditionStartEvent(condition);
						events.add(event);
					} else {
						condition.addObserver(this);
						immature.add(condition);
					}
				}
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
			if (logicModule.getLogicModule() instanceof SimpleLogicModule) {
				if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()) {
					if (condition.isMature()) {
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
			}

	}

	@Override
	public void registerUpdate(Condition condition) {
		LogicModuleRegistry logicModule = condition.getLogicModule();
		if (logicModule == null) {
			throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Condition has no logic module assigned");
		}
		if (condition.isShow())

			if (logicModule.getLogicModule() instanceof SimpleLogicModule) {
				if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()) {
					logger.debug("| " + logicModule);
				}
			}

	}

	public List<ConditionEvent> getEvents() {
		return events;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Condition) {
			Condition c = (Condition) o;
			if (immature.contains(c)) {
				logger.info("Immature condition changed: " + c.getTitle());
				if (c.isMature()) {
					logger.info("Is now mature: " + c.getTitle());
					immature.remove(c);
					events.add(generateConditionStartEvent(c));
				}
			}
		}
	}

}
