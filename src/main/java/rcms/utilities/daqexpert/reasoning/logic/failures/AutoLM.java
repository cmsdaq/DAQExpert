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

    private final String ERROR_STATE = "ERROR";

    public AutoLM() {
        this.name = "Test problem";

        this.description = "Subsystem {{SUBSYSTEM}} is in {{STATE}}";

        this.action = new SimpleAction("Look to the left", "Look to the right", "<<RedRecycle::{{SUBSYSTEM}}>>","<<GreenRecycle::{{SUBSYSTEM}}>>", "Make an e-log entry");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        SubSystem tracker = daq.getSubSystems().stream().filter(s->s.getName().equalsIgnoreCase("TRACKER")).findFirst().orElse(null);


        if (tracker != null && tracker.getStatus().equalsIgnoreCase("faulty")) {

            contextHandler.registerObject("SUBSYSTEM", tracker,s->s.getName());
            contextHandler.register("STATE", tracker.getStatus());
            return true;
        }
        return false;
    }
}
