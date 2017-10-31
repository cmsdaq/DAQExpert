package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.Map;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromHlt extends KnownFailure {

    private static final Logger logger = Logger.getLogger(BackpressureFromHlt.class);

    public BackpressureFromHlt() {
        this.name = "Backpressure from HLT";

        this.description = "DAQ backpressure coming from Filter Farm.";

        ConditionalAction action = new ConditionalAction("Call the DAQ DOC");
        action.addContextSteps("high-output-rate", "Are we running with the correct pre-scale column?", "Talk to the trigger shifter and shift leader.", "You may need to call HLT DOC.");
        action.addContextSteps("cmssw-crashing", "Call the HLT DOC, mentioning the messages you see under HLT Alerts in F3 Mon.", "Call the DAQ DOC. He might need to clean up the Filter Farm.");
        action.addContextSteps("hlt-cpu-high-usage", "Are we running with the correct pre-scale column?", "Talk to the trigger shifter and shift leader.", "You may need to call the HLT DOC.");

        this.action = action;

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        assignPriority(results);
        boolean result = false;

        return result;
    }

}
