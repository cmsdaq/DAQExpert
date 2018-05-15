package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.*;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.basic.TmpUpgradedFedProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.*;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromEventBuilding extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(BackpressureFromEventBuilding.class);

    private static Integer fedBackpressureThreshold;
    private Integer evmFewRequestsThreshold;
    private Integer deadtimeThresholdInPercentage;

    public BackpressureFromEventBuilding() {
        this.name = "Backpressure from Event Builder";

        this.description = "Backpressure from Event Building (i.e. not from HLT). " +
                "Exists FEDBuilders with backpressure to FEDs ({{P}}) and 0 requests on RU, 256 fragments in RU. " +
                "EVM has few ({{EVM-REQUESTS}}, the threshold is <100) requests. All BUs are enabled.";

        this.action = new SimpleAction("Call the DAQ on-call mentioning that we have backpressure from the event building.");

    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.FedDeadtimeDueToDaq);
        require(LogicModuleRegistry.TmpUpgradedFedProblem);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean fedDeadtimeDueToDAQ = results.get(FedDeadtimeDueToDaq.class.getSimpleName()).getResult();
        boolean tmpUpgradedFedBackpressured = results.get(TmpUpgradedFedProblem.class.getSimpleName()).getResult();


        if(fedDeadtimeDueToDAQ || tmpUpgradedFedBackpressured) {

            assignPriority(results);
            boolean result = false;

            Iterator<RU> i = daq.getRus().iterator();
            Set<RU> problematicRus = new HashSet<>();
            Set<FED> problematicFeds = new HashSet<>();
            while (i.hasNext()) {
                RU ru = i.next();
                if (ru.getRequests() == 0 && ru.getFragmentsInRU() == 256) {

                    boolean foundProblematicFeds = false;
                    for (FED fed : ru.getFEDs(false)) {


                        //TODO: LATER: looking at dead time of FED. need to take into account FED - pseudoFED relationship.
                        if (!fed.isFrlMasked()) {

                            //TODO: use the result of other LMs instead of repeating the job
                            if (fed.getPercentWarning() + fed.getPercentBusy() > deadtimeThresholdInPercentage) {

                                float backpressure = fed.getPercentBackpressure();
                                if (backpressure > fedBackpressureThreshold) {

                                    logger.debug("Found problematic FED: " + fed.getSrcIdExpected());
                                    contextHandler.register("PROBLEMATIC-FED", fed.getSrcIdExpected());
                                    contextHandler.registerForStatistics("BACKPRESSURE", backpressure);
                                    problematicFeds.add(fed);
                                    foundProblematicFeds = true;
                                }
                            }

                        }
                    }
                    if (foundProblematicFeds) {
                        contextHandler.register("PROBLEMATIC-RU", ru.getHostname());
                        logger.debug("Found problematic RU: " + ru.getHostname());
                        problematicRus.add(ru);
                    }


                }
            }

            boolean evmFewRequests = false;
            boolean allBusEnabled = true;

            for (RU ru : daq.getRus()) {
                if (ru.isEVM() && ru.getRequests() < evmFewRequestsThreshold) {
                    logger.trace("EVM has: " + ru.getRequests() + " requests");
                    contextHandler.registerForStatistics("EVM-REQUESTS", ru.getRequests());
                    evmFewRequests = true;
                }
            }

            for (BU bu : daq.getBus()) {
                logger.trace("Bu state: " + bu.getStateName());
                if (!"Enabled".equalsIgnoreCase(bu.getStateName())) {
                    allBusEnabled = false;
                }
            }


            if (problematicFeds.size() > 0 && problematicRus.size() > 0 && evmFewRequests && allBusEnabled) {
                result = true;
            }

            return result;
        }else {
            return false;
        }
    }

    @Override
    public void parametrize(Properties properties) {
        this.deadtimeThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
        this.fedBackpressureThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.evmFewRequestsThreshold = FailFastParameterReader.getIntegerParameter(properties,Setting.EXPERT_LOGIC_EVM_FEW_EVENTS, this.getClass());
    }
}
