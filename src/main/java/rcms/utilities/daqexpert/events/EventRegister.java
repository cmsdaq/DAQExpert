package rcms.utilities.daqexpert.events;

import java.util.List;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

/**
 * Interface for event registers used in processing of condition stream. This
 * interface is used to register events on condition starts, updates and ends.
 * 
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface EventRegister {

	public void registerBegin(LogicModuleRegistry logicModule, Condition condition);

	public void registerEnd(LogicModuleRegistry logicModule, Condition condition);

	public void registerUpdate(LogicModuleRegistry logicModule, Condition condition);

	public List<ConditionEvent> getEvents();
}
