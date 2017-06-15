package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;

public class StuckAfterSoftError extends KnownFailure {

	private String precedingState = "";
	private String previousState = "";
	private final String precedingStateToToggle = "FixingSoftError";
	private final String stateToToggle = "Error";

	private final static Logger logger = Logger.getLogger(StuckAfterSoftError.class);

	public StuckAfterSoftError() {
		this.name = "Stuck after soft error";
		this.description = "Stuck after soft error";
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean result = false;
		String currentState = daq.getLevelZeroState();

		logger.trace("Checking condition: " + precedingState + ", current state: " + currentState);

		if (precedingStateToToggle.equalsIgnoreCase(precedingState) && stateToToggle.equalsIgnoreCase(currentState)) {

			logger.debug("Condition satisfied: " + precedingState + ", current state: " + currentState);
			result = true;
		}

		if (!previousState.equalsIgnoreCase(currentState)) {
			logger.debug("Changing state from: " + precedingState + " to " + currentState);
			precedingState = previousState;
		}

		previousState = currentState;
		return result;
	}

}
