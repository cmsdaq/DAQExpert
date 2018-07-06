package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.TmpUpgradedFedProblem;

import java.util.Map;

public class FedStuckDueToDaq extends KnownFailure {

    public FedStuckDueToDaq(){
        this.name = "FED stuck due to DAQ";
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

        // Conditions:
        // - backprssure > 80%
        // - warning stuck;
        return false;
    }
}
