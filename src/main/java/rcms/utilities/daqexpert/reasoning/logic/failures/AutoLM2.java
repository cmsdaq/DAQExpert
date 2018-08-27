package rcms.utilities.daqexpert.reasoning.logic.failures;


import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.util.Map;


public class AutoLM2 extends KnownFailure {

    private final String ERROR_STATE = "ERROR";

    public AutoLM2() {
        this.name = "L0 problem";

        this.description = "L0 is in error state";

        this.action = new SimpleAction("<<TTCHardReset>>, will not help. Try if you don't believe", "Some say <<StopAndStartTheRun>> will help");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        if ("Error".equalsIgnoreCase(daq.getLevelZeroState())) {
            return true;
        }
        return false;
    }
}
