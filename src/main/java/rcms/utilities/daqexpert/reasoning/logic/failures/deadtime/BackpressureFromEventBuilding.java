package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.Map;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromEventBuilding extends KnownFailure {

    private static final Logger logger = Logger.getLogger(BackpressureFromEventBuilding.class);

    public BackpressureFromEventBuilding() {
        this.name = "Backpressure from Event Builder";

        this.description = "Backpressure from Event Building (i.e. from RU but not from BUs)";

        this.action = new SimpleAction("Call Remi or Andre.");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        assignPriority(results);
        boolean result = false;

        return result;
    }

}
