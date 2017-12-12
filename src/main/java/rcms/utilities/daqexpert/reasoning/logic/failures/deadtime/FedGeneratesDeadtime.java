package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ReusableContextEntry;
import rcms.utilities.daqexpert.processing.context.functions.FedPrinter;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.*;
import java.util.stream.Collectors;

public class FedGeneratesDeadtime extends KnownFailure implements Parameterizable {

    private float deadtimeThresholdInPercentage;
    private float backpressureThresholdInPercentage;

    private static final Logger logger = Logger.getLogger(FedGeneratesDeadtime.class);

    public FedGeneratesDeadtime() {
        this.name = "FED problem";
    }

    @Override
    public void declareRequired(){
        require(LogicModuleRegistry.FEDDeadtime);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        Output fedDeadtimeOutput= results.get(FEDDeadtime.class.getSimpleName());
        if (!fedDeadtimeOutput.getResult()) {
            return false;
        }

        boolean result = false;

        ReusableContextEntry<FED> reusableContextEntry = fedDeadtimeOutput.getContext().getReusableContextEntry("PROBLEM-FED");

        if(reusableContextEntry == null){
            return false;
        }

        Set<FED> fedsWithDeadtime = reusableContextEntry.getObjectSet();

        for(FED fedWithDeadtime : fedsWithDeadtime){
            Set<FED> fedsBehind = FEDHierarchyRetriever.getFedsBehindPseudo(fedWithDeadtime);
            float backpressure = 0;
            Set<FED> problematicFedsBehindPseudoFed = null;

            if(fedsBehind == null || fedsBehind.size()==0){
                backpressure = fedWithDeadtime.getPercentBackpressure();
            } else{
                // get maximum backpressure value
                backpressure = fedsBehind.stream().max(
                        Comparator.comparing(FED::getPercentBackpressure)).get().getPercentBackpressure();
                problematicFedsBehindPseudoFed = fedsBehind.stream().filter(
                        f -> f.getPercentBackpressure() < backpressureThresholdInPercentage
                ).collect(Collectors.toSet());
            }
            if (backpressure < backpressureThresholdInPercentage) {
                result = true;

                if(problematicFedsBehindPseudoFed == null) {
                    contextHandler.registerObject("PROBLEM-FED", fedWithDeadtime, new FedPrinter());
                } else{
                    for(FED fed: problematicFedsBehindPseudoFed){
                        contextHandler.registerObject("PROBLEM-FED", fed, f -> "FED" + f.getSrcIdExpected());
                    }
                }
                contextHandler.registerForStatistics("BACKPRESSURE", backpressure, "%", 1);

            }
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.deadtimeThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
        this.backpressureThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "FED {{PROBLEM-FED}} generates deadtime {{DEADTIME}}, the threshold is " + deadtimeThresholdInPercentage + "%. There is no backpressure from DAQ on this FED.";
    }

}
