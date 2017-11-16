package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class FedGeneratesDeadtime extends KnownFailure implements Parameterizable {

    private float deadtimeThresholdInPercentage;
    private float backpressureThresholdInPercentage;

    private static final Logger logger = Logger.getLogger(FedGeneratesDeadtime.class);

    public FedGeneratesDeadtime() {
        this.name = "FED problem";
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean fedDeadtime = results.get(FEDDeadtime.class.getSimpleName());
        if (!fedDeadtime) {
            return false;
        }

        boolean result = false;


        for (TTCPartition partition : daq.getTtcPartitions()) {

            Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(partition);
            float deadPercentage = 0;
            float backpressure = 0;

            for (Map.Entry<FED, Set<FED>> fedGroup : h.entrySet()) {

                FED topLevelFed = fedGroup.getKey();
                Set<FED> problematicFedsBehindPseudoFed = null;

                if (fedGroup.getValue()==null || fedGroup.getValue().size() == 0) {
                    // flat hierarchy

                    backpressure = topLevelFed.getPercentBackpressure();
                    deadPercentage += topLevelFed.getPercentBusy();
                    deadPercentage += topLevelFed.getPercentWarning();

                } else {
                    // Exists pseudo feds
                    deadPercentage += topLevelFed.getPercentBusy();
                    deadPercentage += topLevelFed.getPercentWarning();
                    Set<FED> feds = fedGroup.getValue();

                    // get maximum backpressure value
                    backpressure = feds.stream().max(
                            Comparator.comparing(FED::getPercentBackpressure)).get().getPercentBackpressure();
                    problematicFedsBehindPseudoFed = feds.stream().filter(
                            f -> f.getPercentBackpressure() < backpressureThresholdInPercentage
                    ).collect(Collectors.toSet());
                }


                if (deadPercentage > deadtimeThresholdInPercentage && backpressure < backpressureThresholdInPercentage) {
                    result = true;

                    if(problematicFedsBehindPseudoFed == null) {
                        context.register("FED", topLevelFed.getSrcIdExpected());
                    } else{
                        for(FED fed: problematicFedsBehindPseudoFed){
                            context.register("FED", fed.getSrcIdExpected());
                        }
                    }
                    context.registerForStatistics("DEADTIME", deadPercentage, "%", 1);
                    context.registerForStatistics("BACKPRESSURE", deadPercentage, "%", 1);

                }
            }


        }
        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.deadtimeThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
        this.backpressureThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "FED {{FED}} generates deadtime {{DEADTIME}}, the threshold is " + deadtimeThresholdInPercentage + "%. There is no backpressure from DAQ on this FED.";
    }

}
