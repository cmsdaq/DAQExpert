package rcms.utilities.daqexpert.reasoning.base;

import java.util.Map;

import rcms.utilities.daqexpert.reasoning.base.action.Action;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

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
	
	protected void assignPriority(Map<String, Boolean> results) {
		boolean stableBeams = results.get(StableBeams.class.getSimpleName());
		this.priority = stableBeams ? ConditionPriority.CRITICAL : ConditionPriority.WARNING;
	}


}
