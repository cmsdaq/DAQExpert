package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.*;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.Map;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromFerol extends KnownFailure {

    private static final Logger logger = Logger.getLogger(BackpressureFromFerol.class);

    public BackpressureFromFerol() {
        this.name = "Backpressure from FEROL/FEDBuilder";

        this.description = "DAQ backpressure coming from FEROL or FEDBuilder.";

        this.action = new SimpleAction("Call the DAQ on-call and mention this message");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        assignPriority(results);
        boolean result = false;

        return result;
    }

}
