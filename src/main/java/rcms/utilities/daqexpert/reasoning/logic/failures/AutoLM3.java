package rcms.utilities.daqexpert.reasoning.logic.failures;


import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.util.Map;


public class AutoLM3 extends KnownFailure {

    private final String ERROR_STATE = "ERROR";

    public AutoLM3() {
        this.name = "Critical test problem";

        this.description = "Subsystem {{SUBSYSTEM}} is in {{STATE}}";

        this.action = new SimpleAction("<<GreenRecycle::{{SUBSYSTEM}}>>");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        SubSystem ecal = daq.getSubSystems().stream().filter(s->s.getName().equalsIgnoreCase("ECAL")).findFirst().orElse(null);
        SubSystem tracker = daq.getSubSystems().stream().filter(s->s.getName().equalsIgnoreCase("TRACKER")).findFirst().orElse(null);


        if (ecal != null
                && ecal.getStatus().equalsIgnoreCase("halted")
                && tracker != null
                && tracker.getStatus().equalsIgnoreCase("halted")) {

            contextHandler.registerObject("SUBSYSTEM", ecal,s->s.getName());
            contextHandler.registerObject("SUBSYSTEM", tracker,s->s.getName());

            contextHandler.register("STATE", "halted");
            return true;
        }
        return false;
    }
}
