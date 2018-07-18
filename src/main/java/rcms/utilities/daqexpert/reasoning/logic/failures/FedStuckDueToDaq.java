package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.Map;
import java.util.Set;

public class FedStuckDueToDaq extends KnownFailure {

    private float backpressureThreshold = 80;

    public FedStuckDueToDaq() {
        this.name = "FED stuck due to DAQ";

        this.description = "The run is blocked by {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}}. " +
                "It receives backpressure from the DAQ. " +
                "There is nothing wrong with this FED, the problem is in the DAQ or downstream. " +
                "There is typically another condition active for the problem in the DAQ";
        this.briefDescription = "FED {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}} is stuck due to DAQ";
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

        boolean result = false;

        // Conditions (with pseduo hierarcyy)
        // - backprssure > 80%
        // - stuck in warning/busy
        for (SubSystem subSystem : daq.getSubSystems()) {

            for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

                if (!ttcp.isMasked()) {
                    boolean includePartition = false;
                    TTSState currentState = getParitionState(ttcp);
                    if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

                        Map<FED, Set<FED>> fedHierarchy = FEDHierarchyRetriever.getFEDHierarchy(ttcp);

                        for (Map.Entry<FED, Set<FED>> fed : fedHierarchy.entrySet()) {

                        /* there are FEDs behind the pseudo FED */
                            if (fed.getValue().size() > 0) {
                                for (FED dep : fed.getValue()) {
                                    if (dep.getPercentBackpressure() > backpressureThreshold) {
                                        includePartition = true;
                                        contextHandler.register("PROBLEM-FED",
                                                "(" + dep.getSrcIdExpected() + " behind pseudo FED "
                                                        + fed.getKey().getSrcIdExpected() + ")");
                                    }
                                }
                            }
                                    /* there are no FEDs in hierarchy */
                            else {
                                if (fed.getKey().getPercentBackpressure() > backpressureThreshold) {
                                    includePartition = true;
                                    contextHandler.register("PROBLEM-FED", fed.getKey().getSrcIdExpected());
                                }

                            }
                        }
                    }

                    if (includePartition) {
                        contextHandler.register("PROBLEM-PARTITION", ttcp.getName());
                        contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());
                        result = true;
                    }
                }
            }
        }


        return result;
    }
}
