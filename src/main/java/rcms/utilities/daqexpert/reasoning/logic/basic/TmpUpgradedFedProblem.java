package rcms.utilities.daqexpert.reasoning.logic.basic;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMMType;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class TmpUpgradedFedProblem extends ContextLogicModule implements Parameterizable {

    private float threshold;

    public TmpUpgradedFedProblem() {
        this.name = "Backpressure on upgraded FED";
        this.priority = ConditionPriority.DEFAULTT;
        this.threshold = 0;
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean expectedRate = false;
        expectedRate = results.get(TTSDeadtime.class.getSimpleName());
        if (!expectedRate)
            return false;

        boolean result = false;


        for (TTCPartition partition : daq.getTtcPartitions()) {

            Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(partition);

            for (Map.Entry<FED, Set<FED>> fedGroup : h.entrySet()) {

                float backpressure = 0;

                FED topLevelFed = fedGroup.getKey();
                Set<FED> problematicFedsBehindPseudoFed = null;

                if (fedGroup.getValue() == null || fedGroup.getValue().size() == 0) {
                    // flat hierarchy
                    backpressure = topLevelFed.getPercentBackpressure();

                } else {
                    // Exists pseudo feds
                    Set<FED> feds = fedGroup.getValue();

                    // get maximum backpressure value
                    backpressure = feds.stream().max(
                            Comparator.comparing(FED::getPercentBackpressure)).get().getPercentBackpressure();
                    problematicFedsBehindPseudoFed = feds.stream().filter(
                            f -> f.getPercentBackpressure() > threshold
                    ).collect(Collectors.toSet());
                }

                if (backpressure > threshold) {
                    result = true;
                    context.registerForStatistics("VALUE", backpressure, "%", 1);
                    if (problematicFedsBehindPseudoFed == null) {
                        context.register("FED", topLevelFed.getSrcIdExpected());
                    } else {
                        for (FED fed : problematicFedsBehindPseudoFed) {
                            context.register("FED", fed.getSrcIdExpected());
                        }
                    }
                    TTCPartition p = topLevelFed.getTtcp();

                    context.register("PARTITION", p != null ? p.getName() : "null");
                    context.register("SUBSYSTEM", p != null ?
                            p.getSubsystem() != null ? p.getSubsystem().getName() : "null"
                            : "null");
                }
            }
        }


        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "High backpressure on fed(s) {{FED}} in partition(s) {{PARTITION}} in subsystem(s) {{SUBSYSTEM}} is {{VALUE}} the threshold is "
                + threshold + "%";

    }

}
