package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.TmpUpgradedFedProblem;

import java.util.Map;

public class FedStuckDueToDaq extends KnownFailure {

    public FedStuckDueToDaq(){
        this.name = "FED stuck due to DAQ";

        this.description = "The run is blocked by {{PROBLEM-FED}}. It receives backpressure from the DAQ. " +
                "There is nothing wrong with this FED, the problem is in the DAQ or downstream. " +
                "There is typically another condition active for the problem in the DAQ";
        this.briefDescription = "FED {{PROBLEM-FED}} is stuck due to DAQ";
        this.action = new SimpleAction("Typically another condition should describe the action. If not, call the DAQ on-call");

    }


    @Override
    public void declareRelations() {
        declareCause(LogicModuleRegistry.CorruptedData);
        declareCause(LogicModuleRegistry.OutOfSequenceData);
        declareCause(LogicModuleRegistry.FlowchartCase1);
        declareCause(LogicModuleRegistry.LinkProblem);
        declareCause(LogicModuleRegistry.FEROLFifoStuck);
        declareCause(LogicModuleRegistry.RuStuck);
        declareCause(LogicModuleRegistry.RuStuckWaiting);
        declareCause(LogicModuleRegistry.OnlyFedStoppedSendingData);
        declareCause(LogicModuleRegistry.RuStuckWaitingOther);

        declareAffected(LogicModuleRegistry.NoRateWhenExpected);
        declareAffected(LogicModuleRegistry.BackpressureFromEventBuilding);
        declareAffected(LogicModuleRegistry.FedDeadtimeDueToDaq);
        declareAffected(LogicModuleRegistry.TmpUpgradedFedProblem);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        // Conditions (with pseduo hierarcyy)
        // - backprssure > 80%
        // - stuck in warning/busy
        return false;
    }
}
