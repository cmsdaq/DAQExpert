package rcms.utilities.daqexpert.events;

import java.util.List;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

public interface EventRegister {

	public void registerBegin(LogicModuleRegistry logicModule, Condition condition);

	public void registerEnd(LogicModuleRegistry logicModule, Condition condition);

	public void registerUpdate(LogicModuleRegistry logicModule, Condition condition);

	public List<Event> getEvents();
}
