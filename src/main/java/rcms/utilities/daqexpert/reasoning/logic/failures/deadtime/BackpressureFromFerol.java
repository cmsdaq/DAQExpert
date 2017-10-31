package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.*;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Logic module identifying the reason behind deadtime
 */
public class BackpressureFromFerol extends KnownFailure {

    private static final Logger logger = Logger.getLogger(BackpressureFromFerol.class);

    public BackpressureFromFerol() {
        this.name = "Backpressure from FEROL/FEDBuilder";

        this.description = "DAQ backpressure coming from FEROL or FEDBuilder. " +
                "FED Builder with backpressure {{BACKPRESSURE}} to FED {{PROBLEMATIC-FED}}. " +
                "Corresponding RU {{PROBLEMATIC-RU}} has more than 0 requests and less than 256 fragments.";

        this.action = new SimpleAction("Call the DAQ on-call and mention this message");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        assignPriority(results);
        boolean result = false;

        Iterator<FED> i = daq.getFeds().iterator();

        Set<RU> rusToCheck = new HashSet<>();
        Set<RU> problematicRus = new HashSet<>();
        Set<FED> problematicFeds = new HashSet<>();

        while (i.hasNext()) {
            FED fed = i.next();
            if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
                float backpressure = fed.getPercentBackpressure();

                if (backpressure > 2) {
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
                if (ru.getRequests() > 0 && ru.getFragmentsInRU() < 256) {
                    logger.trace("Found problematic RU: " + ru.getHostname());
                    context.register("PROBLEMATIC-RU", ru.getHostname());
                    problematicRus.add(ru);
                }
            }
        }

        if (problematicFeds.size() > 0 && problematicRus.size() > 0) {
            result = true;
        }

        return result;
    }

}
