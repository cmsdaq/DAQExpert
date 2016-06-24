package rcms.utilities.daqexpert.reasoning.base;

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
	protected String action;

	/**
	 * Condition description
	 */
	protected String description;

	/**
	 * Update entry with useful details related to this condition
	 * 
	 * @param daq
	 *            current DAQ snapshot
	 * @param entry
	 *            current analysis entry to be updated
	 */
	public abstract void gatherInfo(DAQ daq, Entry entry);

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
