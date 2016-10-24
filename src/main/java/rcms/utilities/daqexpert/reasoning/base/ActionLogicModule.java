package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqexpert.reasoning.base.action.Action;

/**
 * The Action Logic Module has additional field - <code>action</code>. It is to be
 * used when identified conditions require an action to take.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ActionLogicModule extends SimpleLogicModule {

	/**
	 * What should be done when LM's condition is satisfied
	 */
	protected Action action;

	/**
	 * Context is used to parameterize action and description fields with
	 * specific context information. Variables will be replaced with values from
	 * this context
	 */
	protected final Context context;

	public ActionLogicModule() {
		this.context = new Context();
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Context getContext() {
		return context;
	}

}
