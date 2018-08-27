package rcms.utilities.daqexpert.reasoning.logic.failures;


import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.Counter;

import java.util.List;
import java.util.Map;


public class AutoLM extends KnownFailure {

    public AutoLM() {
        this.name = "Subystem in faulty state";

        this.description = "Subsystem(s) {{SUBSYSTEM}} is in {{STATE}}";

        this.action = new SimpleAction("<<TTCHardReset>> will not help",
                "<<StopAndStartTheRun>> might help",
                "<<GreenRecycle::{{SUBSYSTEM}}>> probably helps",
                "<<RedRecycle::{{SUBSYSTEM}}>> will resolve the problem");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean result = false;

        for(SubSystem subsystem : daq.getSubSystems()) {
            if (subsystem != null && subsystem.getStatus().equalsIgnoreCase("faulty")) {

                contextHandler.registerObject("SUBSYSTEM", subsystem, s -> s.getName());
                contextHandler.register("STATE", subsystem.getStatus());
                result = true;
            }
        }
        return result;
    }
}
