package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.*;
import java.util.stream.Collectors;

public class FedDeadtimeDueToDaq extends KnownFailure implements Parameterizable {

    private float deadtimeThresholdInPercentage;
    private float backpressureThresholdInPercentage;

    private static final Logger logger = Logger.getLogger(FedDeadtimeDueToDaq.class);

    public FedDeadtimeDueToDaq() {
        this.name = "FED deadtime due to DAQ";
    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.FEDDeadtime);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean fedDeadtime = results.get(FEDDeadtime.class.getSimpleName()).getResult();
        if (!fedDeadtime) {
            return false;
        }
        boolean result = false;


        for (TTCPartition partition : daq.getTtcPartitions()) {

            Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(partition);

            logger.debug(partition.getName() + " has " + h.entrySet().size() + " FEDs (FED groups)");
            for (Map.Entry<FED, Set<FED>> fedGroup : h.entrySet()) {

                logger.debug("FED " + fedGroup.getKey());

                float deadPercentage = 0;
                float backpressure = 0;

                FED topLevelFed = fedGroup.getKey();
                Set<FED> problematicFedsBehindPseudoFed = null;

                if (fedGroup.getValue()==null || fedGroup.getValue().size() == 0) {
                    // flat hierarchy
                    logger.debug("No FED hierachy: " + topLevelFed);
                    backpressure = topLevelFed.getPercentBackpressure();
                    deadPercentage += topLevelFed.getPercentBusy();
                    deadPercentage += topLevelFed.getPercentWarning();

                } else {
                    logger.debug("A FED hierarchy: " + topLevelFed);
                    // Exists pseudo feds
                    deadPercentage += topLevelFed.getPercentBusy();
                    deadPercentage += topLevelFed.getPercentWarning();
                    Set<FED> feds = fedGroup.getValue();

                    // get maximum backpressure value
                    backpressure = feds.stream().max(
                            Comparator.comparing(FED::getPercentBackpressure)).get().getPercentBackpressure();
                    problematicFedsBehindPseudoFed = feds.stream().filter(
                            f -> f.getPercentBackpressure() > backpressureThresholdInPercentage
                    ).collect(Collectors.toSet());
                }


                if (deadPercentage > deadtimeThresholdInPercentage && backpressure > backpressureThresholdInPercentage) {
                    result = true;

                    if(problematicFedsBehindPseudoFed == null) {
                        contextHandler.register("PROBLEM-FED", topLevelFed.getSrcIdExpected());
                    } else{
                        for(FED fed: problematicFedsBehindPseudoFed){
                            contextHandler.register("PROBLEM-FED", fed.getSrcIdExpected());
                        }
                    }
                    contextHandler.registerForStatistics("DEADTIME", deadPercentage, "%", 1);
                    contextHandler.registerForStatistics("BACKPRESSURE", deadPercentage, "%", 1);

                }
            }


        }
        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.deadtimeThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
        this.backpressureThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "FED {{PROBLEM-FED}} has a deadtime {{DEADTIME}}, due to DAQ backpressure {{BACKPRESSURE}}. " +
                "The threshold for deadtime is " + deadtimeThresholdInPercentage + "%, backpressure: " + backpressureThresholdInPercentage + "%";
        this.briefDescription = "FED {{PROBLEM-FED}} has a deadtime {{DEADTIME}}, due to DAQ backpressure {{BACKPRESSURE}}.";
    }

}
