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


	public Action getAction() {
		return action;
	}

	@Override
	public void declareRelations() {
		require(LogicModuleRegistry.StableBeams);
	}

	protected void assignPriority(Map<String, Output> results) {
		boolean stableBeams = results.get(StableBeams.class.getSimpleName()).getResult();
		this.priority = stableBeams ? ConditionPriority.CRITICAL : ConditionPriority.WARNING;
	}

	/**
	 * Returns final readable version of action steps.
	 * @return final readlable action steps
	 */
	public List<String> getActionWithContext() {
		return this.getContextHandler().getActionWithContext(this.action);
	}

	/**
	 * Returns action steps with context information but without Automatic recovery action replaced. Used for automatic recovery action building
	 * @return action steps with readable context but raw recovery action
	 */
	public List<String> getActionWithContextRawRecovery() {
		this.getContextHandler().setHighlightMarkup(false);
		return this.getContextHandler().getActionWithContext(this.action,false);
	}

	public boolean isAutomationAvailable(){
		return this.getContextHandler().isAutomationAvailable(this.action);
	}


}
