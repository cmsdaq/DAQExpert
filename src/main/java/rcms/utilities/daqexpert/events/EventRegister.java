package rcms.utilities.daqexpert.events;

import java.util.List;

import rcms.utilities.daqexpert.persistence.Condition;

/**
 * Interface for event registers used in processing of condition stream. This
 * interface is used to register events on condition starts, updates and ends.
 * 
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface EventRegister {

	public void registerBegin(Condition condition);

	public void registerEnd(Condition condition);

	public void registerUpdate(Condition condition);

	public List<ConditionEvent> getEvents();
}
