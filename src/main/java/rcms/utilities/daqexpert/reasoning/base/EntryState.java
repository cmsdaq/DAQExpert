package rcms.utilities.daqexpert.reasoning.base;

/**
 * States of entries (analysis results stream)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum EntryState {

	/** The entry is new - potential candidate for notification */
	NEW,

	/**
	 * The entry is mature (duration is longer than defined threshold) -
	 * notification will be generated
	 */
	MATURE,

	/**
	 * Start notification for the entry has been generated - entry is still
	 * active
	 */
	STARTED,

	/**
	 * Entry is not active any more - End notification will be generated
	 */
	FINISHED;
}
