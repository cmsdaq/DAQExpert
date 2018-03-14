package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.action.Action;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

import java.util.List;
import java.util.Map;

/**
 * The Action Logic Module has additional field - <code>action</code>. It is to be
 * used when identified conditions require an action to take.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ActionLogicModule extends ContextLogicModule {

	/**
	 * What should be done when LM's condition is satisfied
	 */
	protected Action action;

	
	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	@Override
	public void declareRequired() {
		require(LogicModuleRegistry.StableBeams);
	}

	protected void assignPriority(Map<String, Output> results) {
		boolean stableBeams = results.get(StableBeams.class.getSimpleName()).getResult();
		this.priority = stableBeams ? ConditionPriority.CRITICAL : ConditionPriority.WARNING;
	}
	
	public List<String> getActionWithContext() {
		return this.getContextHandler().getActionWithContext(this.action);
	}


}
