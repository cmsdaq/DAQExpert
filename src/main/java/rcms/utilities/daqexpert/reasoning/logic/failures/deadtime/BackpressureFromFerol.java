package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
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
public class BackpressureFromFerol extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(BackpressureFromFerol.class);

    private static Integer fedBackpressureThreshold;

    private Integer evmFewRequestsThreshold;

    public BackpressureFromFerol() {
        this.name = "Backpressure from FEROL/FEDBuilder";

        this.description = "DAQ backpressure coming from FEROL or FEDBuilder. " +
                "FED Builder with backpressure {{BACKPRESSURE}} to FED {{PROBLEMATIC-FED}}. " +
                "Corresponding RU {{PROBLEMATIC-RU}} has more than 0 requests and less than 256 fragments.";

        this.action = new SimpleAction("Call the DAQ on-call and mention this message");


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
                if (ru.getRequests() > 0 && ru.getFragmentsInRU() < 256) {

                    boolean foundProblematicFeds = false;
                    for (FED fed : ru.getFEDs(false)) {


                        //TODO: Later also check if the FED (or its pseudo-FED) has dead time above a threshold.
                        if (!fed.isFrlMasked()) {

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
                    if (foundProblematicFeds) {
                        contextHandler.register("PROBLEMATIC-RU", ru.getHostname());
                        logger.debug("Found problematic RU: " + ru.getHostname());
                        problematicRus.add(ru);
                    }


                }
            }


            if (problematicFeds.size() > 0 && problematicRus.size() > 0) {
                result = true;
            }

            return result;
        }else {
            return false;
        }
    }

    @Override
    public void parametrize(Properties properties) {
        this.fedBackpressureThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.evmFewRequestsThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_EVM_FEW_EVENTS, this.getClass());

    }
}
