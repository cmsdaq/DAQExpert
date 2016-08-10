package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqexpert.reasoning.base.action.Action;

/**
 * Extended condition have additional fields (action and description)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ExtendedCondition extends Condition {

	/**
	 * What should be done when condition is meet
	 */
	protected Action action;


	/**
	 * Context is used to parameterize action and description fields with
	 * specific context information. Variables will be replaced with values from
	 * this context
	 */
	protected final ContextCollector context;

	public ExtendedCondition() {
		this.context = new ContextCollector();
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
	
	public ContextCollector getContext() {
		return context;
	}

}
