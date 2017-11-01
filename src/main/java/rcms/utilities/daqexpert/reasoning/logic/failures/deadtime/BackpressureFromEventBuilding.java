package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.Transition;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.*;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromEventBuilding extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(BackpressureFromEventBuilding.class);

    private static Integer fedBackpressureThreshold;
    private Integer evmFewRequestsThreshold;

    public BackpressureFromEventBuilding() {
        this.name = "Backpressure from Event Builder";

        this.description = "Backpressure from Event Building (i.e. from RU but not from BUs)";

        this.action = new SimpleAction("Call Remi or Andre.");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean transition;
        boolean expectedRate;
        boolean noRateWhenExpected;
        expectedRate = results.get(ExpectedRate.class.getSimpleName());
        transition = results.get(Transition.class.getSimpleName());
        noRateWhenExpected = results.get(NoRateWhenExpected.class.getSimpleName());

        if (!(expectedRate && !transition && !noRateWhenExpected)) {
            return false;
        }

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


                    if (!fed.isFmmMasked() && !fed.isFrlMasked()) {

                        float backpressure = fed.getPercentBackpressure();
                        if (backpressure > fedBackpressureThreshold) {

                            logger.debug("Found problematic FED: " + fed.getSrcIdExpected());
                            context.register("PROBLEMATIC-FED", fed.getSrcIdExpected());
                            context.registerForStatistics("BACKPRESSURE", backpressure);
                            problematicFeds.add(fed);
                            foundProblematicFeds = true;
                        }

                    }
                }
                if (foundProblematicFeds) {
                    context.register("PROBLEMATIC-RU", ru.getHostname());
                    logger.debug("Found problematic RU: " + ru.getHostname());
                    problematicRus.add(ru);
                }


            }
        }

        boolean evmFewRequests = false;
        boolean allBusEnabled = true;

        for(RU ru : daq.getRus()){
            if(ru.isEVM() && ru.getRequests() < evmFewRequestsThreshold){
                logger.trace("EVM has: " + ru.getRequests() + " requests");
                evmFewRequests = true;
            }
        }

        for(BU bu: daq.getBus()){
            logger.trace("Bu state: " + bu.getStateName());
            if(!"Enabled".equalsIgnoreCase(bu.getStateName())){
                allBusEnabled = false;
            }
        }



        if (problematicFeds.size() > 0 && problematicRus.size() > 0 && evmFewRequests && allBusEnabled) {
            result = true;
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.fedBackpressureThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.evmFewRequestsThreshold = FailFastParameterReader.getIntegerParameter(properties,Setting.EXPERT_LOGIC_EVM_FEW_EVENTS, this.getClass());
    }
}
