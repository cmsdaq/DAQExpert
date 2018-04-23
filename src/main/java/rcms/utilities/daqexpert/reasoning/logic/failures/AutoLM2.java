package rcms.utilities.daqexpert.reasoning.logic.failures;


import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.util.Map;


public class AutoLM2 extends KnownFailure {

    private final String ERROR_STATE = "ERROR";

    public AutoLM2() {
        this.name = "More important test problem";

        this.description = "Subsystem {{SUBSYSTEM}} is in {{STATE}}";

        this.action = new SimpleAction("<<RedRecycle::{{SUBSYSTEM}}>>");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        SubSystem ecal = daq.getSubSystems().stream().filter(s->s.getName().equalsIgnoreCase("ECAL")).findFirst().orElse(null);


        if (ecal != null && ecal.getStatus().equalsIgnoreCase("faulty")) {

            contextHandler.registerObject("SUBSYSTEM", ecal,s->s.getName());
            contextHandler.register("STATE", ecal.getStatus());
            return true;
        }
        return false;
    }
}
