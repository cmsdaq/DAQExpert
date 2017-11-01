package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.*;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromHlt extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(BackpressureFromHlt.class);

    private Float fractionBusEnabledThreshold;
    private Integer evmRequestsThreshold;
    private static Integer fedBackpressureThreshold;

    public BackpressureFromHlt() {
        this.name = "Backpressure from HLT";


        ConditionalAction action = new ConditionalAction("Call the DAQ DOC");
        action.addContextSteps("high-output-rate", "Are we running with the correct pre-scale column?", "Talk to the trigger shifter and shift leader.", "You may need to call HLT DOC.");
        action.addContextSteps("cmssw-crashing", "Call the HLT DOC, mentioning the messages you see under HLT Alerts in F3 Mon.", "Call the DAQ DOC. He might need to clean up the Filter Farm.");
        action.addContextSteps("hlt-cpu-high-usage", "Are we running with the correct pre-scale column?", "Talk to the trigger shifter and shift leader.", "You may need to call the HLT DOC.");

        this.action = action;

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        if (results.get(NoRateWhenExpected.class.getSimpleName()))
            return false;

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
            if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
                float backpressure = fed.getPercentBackpressure();

                if (backpressure > fedBackpressureThreshold) {
                    logger.trace("Found problematic FED: " + fed.getSrcIdExpected());
                    problematicFeds.add(fed);
                    context.register("PROBLEMATIC-FED", fed.getSrcIdExpected());
                    context.registerForStatistics("BACKPRESSURE", backpressure);
                    rusToCheck.add(fed.getRu());
                }
            }
        }

        if (rusToCheck.size() > 0) {
            for (RU ru : rusToCheck) {
                if (ru.getRequests() == 0 && ru.getFragmentsInRU() == 256) {
                    logger.trace("Found problematic RU: " + ru.getHostname());
                    context.register("PROBLEMATIC-RU", ru.getHostname());
                    problematicRus.add(ru);
                }
            }
        }

        for (RU ru : daq.getRus()) {
            if (ru.isEVM() && ru.getRequests() < evmRequestsThreshold) {
                logger.trace("EVM has: " + ru.getRequests() + " requests");
                context.registerForStatistics("", ru.getRequests(), "", 1);
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
        float fractionNotEnabled = (allBus - enabledBus) / allBus;

        if (evmFewRequests && fractionNotEnabled > fractionBusEnabledThreshold) {
            context.registerForStatistics("BUSFRACTION", 100 * fractionNotEnabled, "%", 1);
            result = true;
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        fedBackpressureThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        fractionBusEnabledThreshold = FailFastParameterReader.getFloatParameter(properties, Setting.EXPERT_LOGIC_BACKPRESSUREFROMHLT_THRESHOLD_BUS, this.getClass());
        evmRequestsThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_EVM_FEW_EVENTS, this.getClass());

        String printableBusThreshold = Math.round(100 * fractionBusEnabledThreshold) + "%";
        this.description = "DAQ backpressure coming from Filter Farm. EVM has few ({{EVMREQUESTS}}, the threshold is <" + evmRequestsThreshold + ") requests. Large fraction ({{BUSFRACTION}}, the threshold is >" + printableBusThreshold + ") of BUs not enabled";
    }
}
