package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.BU;
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
public class BackpressureFromHlt extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(BackpressureFromHlt.class);
    private static Integer fedBackpressureThreshold;
    private Float fractionBusEnabledThreshold;
    private Integer evmRequestsThreshold;

    public BackpressureFromHlt() {
        this.name = "Backpressure from HLT";

        this.action = new SimpleAction("Check if (more than a few) FUs are crashing by looking at the \"#FUs crash\" column in DAQView and by looking at the HLT Alerts in F3Mon. Contact the HLT DOC and DAQ DOC if you see crashes.",
                "Check the HLT utilization by looking at the Microstates Time chart in F3 Mon. If the HLT is fully occupied, check with the shift crew whether we are running in the right pre-scale column. You may need to call the HLT DOC.",
                "Check the HLT Output rate in F3Mon (There should be a separate warning message if it is too high. Follow the instructions in this separate message).");

    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.FedDeadtimeDueToDaq);
        require(LogicModuleRegistry.TmpUpgradedFedProblem);


        declareAffected(LogicModuleRegistry.FEDDeadtime);
        declareAffected(LogicModuleRegistry.TTSDeadtime);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean fedDeadtimeDueToDAQ = results.get(FedDeadtimeDueToDaq.class.getSimpleName()).getResult();
        boolean tmpUpgradedFedBackpressured = results.get(TmpUpgradedFedProblem.class.getSimpleName()).getResult();


        if (fedDeadtimeDueToDAQ || tmpUpgradedFedBackpressured) {

            assignPriority(results);
            boolean result = false;


            Set<RU> rusToCheck = new HashSet<>();
            Set<RU> problematicRus = new HashSet<>();
            Set<FED> problematicFeds = new HashSet<>();
            boolean evmFewRequests = false;
            boolean allBusEnabled = true;
            Iterator<FED> i = daq.getFeds().iterator();

            while (i.hasNext()) {
                FED fed = i.next();
                if (!fed.isFrlMasked()) {
                    float backpressure = fed.getPercentBackpressure();

                    if (backpressure > fedBackpressureThreshold) {
                        logger.trace("Found problematic FED: " + fed.getSrcIdExpected());
                        problematicFeds.add(fed);
                        contextHandler.register("PROBLEMATIC-FED", fed.getSrcIdExpected());
                        contextHandler.registerForStatistics("BACKPRESSURE", backpressure);
                        rusToCheck.add(fed.getRu());
                    }
                }
            }

            if (rusToCheck.size() > 0) {
                for (RU ru : rusToCheck) {
                    if (ru.getRequests() == 0 && ru.getFragmentsInRU() == 256) {
                        logger.trace("Found problematic RU: " + ru.getHostname());
                        contextHandler.register("PROBLEMATIC-RU", ru.getHostname());
                        problematicRus.add(ru);
                    }
                }
            }

            for (RU ru : daq.getRus()) {
                if (ru.isEVM() && ru.getRequests() < evmRequestsThreshold) {
                    logger.trace("EVM has: " + ru.getRequests() + " requests");
                    contextHandler.registerForStatistics("EVMREQUESTS", ru.getRequests(), "", 1);
                    evmFewRequests = true;
                }
            }


            int enabledBus = 0;
            int allBus = daq.getBus().size();
            for (BU bu : daq.getBus()) {
                logger.trace("Bu state: " + bu.getStateName());
                if ("Enabled".equalsIgnoreCase(bu.getStateName())) {
                    enabledBus++;
                }
            }
            float fractionNotEnabled = ((float) (allBus - enabledBus)) / allBus;

            if (evmFewRequests && fractionNotEnabled > fractionBusEnabledThreshold) {
                contextHandler.registerForStatistics("BUSFRACTION", 100 * fractionNotEnabled, "%", 1);
                result = true;
            }

            return result;
        } else {
            return false;
        }

    }

    @Override
    public void parametrize(Properties properties) {

        fedBackpressureThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        fractionBusEnabledThreshold = FailFastParameterReader.getFloatParameter(properties, Setting.EXPERT_LOGIC_BACKPRESSUREFROMHLT_THRESHOLD_BUS, this.getClass());
        evmRequestsThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_EVM_FEW_EVENTS, this.getClass());

        String printableBusThreshold = Math.round(100 * fractionBusEnabledThreshold) + "%";
        this.description = "DAQ backpressure coming from Filter Farm. EVM has few ({{EVMREQUESTS}} requests, the threshold is <" + evmRequestsThreshold + ") requests. Large fraction ({{BUSFRACTION}}, the threshold is >" + printableBusThreshold + ") of BUs not enabled";
    }
}
