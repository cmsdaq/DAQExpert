package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

public class StuckAfterSoftError extends KnownFailure {

    public StuckAfterSoftError() {
        this.name = "Stuck after fixing-soft-error";
        this.description = "Level zero is stuck after fixing soft error. This is caused by subsystem(s) {{SUBSYSTEM}}";
        this.action = new SimpleAction("Call the DOC of subsystem {{SUBSYSTEM}}", "Ask the DCS shifter to check the status of subsystem {{SUBSYSTEM}}");
    }

    private final static Logger logger = Logger.getLogger(StuckAfterSoftError.class);
    /**
     * Required preceding state of L0 to toggle this logic module. It is
     * followed by <code>stateToToggle</code>
     *
     * @see StuckAfterSoftError#stateToToggle
     */
    private final String precedingStateToToggle = "FixingSoftError";
    /**
     * Required state of L0 to toggle this logic module. It follows the
     * <code>precedingStateToToggle</code>
     *
     * @see StuckAfterSoftError#precedingStateToToggle
     */
    private final String stateToToggle = "Error";
    /**
     * Previous L0 state
     */
    private String previousState = "";
    /**
     * Preceding L0 state - always different than current state
     */
    private String precedingState = "";
    /**
     * List of problematic states that will be used in report
     */
    private List<String> problemStates = Arrays.asList("Error");



    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean result = false;
        String currentState = daq.getLevelZeroState();

        if(currentState == null){
            precedingState = "";
            previousState = "";
            return false;
        }


        if (!previousState.equalsIgnoreCase(currentState)) {
            logger.debug("Changing state from: " + previousState + " to " + currentState);
            precedingState = previousState;
        }

        logger.trace("Checking condition: " + precedingState + ", current state: " + currentState);

        if (precedingStateToToggle.equalsIgnoreCase(precedingState) && stateToToggle.equalsIgnoreCase(currentState)) {

            logger.debug("Condition satisfied: " + precedingState + ", current state: " + currentState);
            result = true;

            for (SubSystem subsystem : daq.getSubSystems()) {
                if (problemStates.contains(subsystem.getStatus())) {
                    context.register("SUBSYSTEM", subsystem.getName());
                }
            }

        }

        previousState = currentState;
        return result;
    }

}
