package rcms.utilities.daqexpert.reasoning.base;

import java.util.List;

import rcms.utilities.daqaggregator.data.DAQ;

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
	protected List<String> action;


	/**
	 * Context is used to parameterize action and description fields with
	 * specific context information. Variables will be replaced with values from
	 * this context
	 */
	protected final ContextCollector context;

	public ExtendedCondition() {
		this.context = new ContextCollector();
	}

	public List<String> getAction() {
		return action;
	}

	public void setAction(List<String> action) {
		this.action = action;
	}
	
	public ContextCollector getContext() {
		return context;
	}

}
