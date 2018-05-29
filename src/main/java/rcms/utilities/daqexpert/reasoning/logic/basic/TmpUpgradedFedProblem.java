package rcms.utilities.daqexpert.reasoning.logic.basic;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class TmpUpgradedFedProblem extends ContextLogicModule implements Parameterizable {

    private float threshold;


    /**
     * Note that module was introduced because of lack of upgraded fed deadtime data.
     */
    public TmpUpgradedFedProblem() {
        this.name = "Upgraded FED problem (TMP)";
        this.priority = ConditionPriority.DEFAULTT;
        this.threshold = 0;
    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.TTSDeadtime);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean ttsDeadtime = false;
        ttsDeadtime = results.get(TTSDeadtime.class.getSimpleName()).getResult();
        if (!ttsDeadtime)
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
                    contextHandler.registerForStatistics("VALUE", backpressure, "%", 1);
                    if (problematicFedsBehindPseudoFed == null) {
                        contextHandler.register("FED", topLevelFed.getSrcIdExpected());
                    } else {
                        for (FED fed : problematicFedsBehindPseudoFed) {
                            contextHandler.register("FED", fed.getSrcIdExpected());
                        }
                    }
                    TTCPartition p = topLevelFed.getTtcp();

                    contextHandler.register("PARTITION", p != null ? p.getName() : "null");
                    contextHandler.register("SUBSYSTEM", p != null ?
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
                + threshold + "%. This does not indicate a problem with these FEDs. This condition is only used as a basis for other backpressure analysis since upgraded FEDs have no deadtime monitoring. For legacy FEDs the deadtime is the basis for backpressure analysis.";

    }

}
