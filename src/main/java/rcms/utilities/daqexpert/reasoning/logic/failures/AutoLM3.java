package rcms.utilities.daqexpert.reasoning.logic.failures;


import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.util.Map;


public class AutoLM3 extends KnownFailure {

    public AutoLM3() {
        this.name = "L0 is ok";

        this.description = "L0 is in Running state so lets ruin this";

        this.action = new SimpleAction("Look left","Look right","If nobody watches issue <<TTCHardReset>> (try up to 2 times)", "If this didn't mess up things: <<StopAndStartTheRun>>");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        if ("Running".equalsIgnoreCase(daq.getLevelZeroState())) {


            contextHandler.register("STATE", daq.getLevelZeroState());
            return true;
        }
        return false;
    }
}
