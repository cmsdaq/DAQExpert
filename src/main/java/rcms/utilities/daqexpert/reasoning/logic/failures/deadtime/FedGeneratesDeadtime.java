package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;
import rcms.utilities.daqexpert.processing.context.StatisticContextEntry;
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
    public void declareRelations(){
        require(LogicModuleRegistry.FEDDeadtime);
        declareCause(LogicModuleRegistry.FlowchartCase5);
        declareAffected(LogicModuleRegistry.FEDDeadtime);
    }

    @Override
    public boolean satisfied(DAQ daq) {

        //assignPriority(results);

        Output fedDeadtimeOutput = getOutputOf(LogicModuleRegistry.FEDDeadtime);

        if (!fedDeadtimeOutput.getResult()) {
            return false;
        }

        boolean result = false;

        ObjectContextEntry<FED> reusableContextEntry = fedDeadtimeOutput.getContext().getReusableContextEntry("PROBLEM-FED");

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


                TTCPartition problemPartition = null;
                if (problematicFedsBehindPseudoFed == null) {
                    contextHandler.registerObject("PROBLEM-FED", fedWithDeadtime, new FedPrinter());
                    problemPartition = fedWithDeadtime.getTtcp();
                } else {
                    for (FED fed : problematicFedsBehindPseudoFed) {
                        contextHandler.registerObject("PROBLEM-FED", fed, f -> "FED" + f.getSrcIdExpected());
                        problemPartition = fed.getTtcp();
                    }
                }
                contextHandler.registerForStatistics("BACKPRESSURE", backpressure, "%", 1);
                contextHandler.registerForStatistics("DEADTIME", fedWithDeadtime.getPercentBusy() + fedWithDeadtime.getPercentWarning(), "%", 1);
                contextHandler.registerObject("PROBLEM-PARTITION", problemPartition, p -> p.getName());
                contextHandler.registerObject("PROBLEM-SUBSYSTEM", problemPartition.getSubsystem(), subSystem -> subSystem.getName());

            }
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.deadtimeThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
        this.backpressureThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "FED {{PROBLEM-FED}} generates deadtime {{DEADTIME}}, the threshold is " + deadtimeThresholdInPercentage + "%. There is no backpressure from DAQ on this FED. FED belongs to partition {{PROBLEM-PARTITION}} in subsystem {{PROBLEM-SUBSYSTEM}}";
        this.briefDescription = "FED(s) {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}}/{{PROBLEM-FED}} generates deadtime {{DEADTIME}}";
    }

}
