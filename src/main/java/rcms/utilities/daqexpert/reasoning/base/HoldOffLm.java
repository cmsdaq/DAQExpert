package rcms.utilities.daqexpert.reasoning.base;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;

import java.util.Map;

public abstract class HoldOffLm extends SimpleLogicModule {


    private final Long holdOffPeriod;

    /**
     * When the condition started to be satisfied - base to calculate holdoff
     */
    private Long start;

    private Long holdOffRelease;

    private static final Logger logger = Logger.getLogger(HoldOffLm.class);

    public HoldOffLm(Long holdOffPeriod) {
        this.holdOffPeriod = holdOffPeriod;
    }


    public boolean satisfiedWithHoldoff(DAQ daq, Map<String, Boolean> results) {
        boolean conditionSatisfied = satisfied(daq, results);

        if (conditionSatisfied) {
            if (start == null) {
                start = daq.getLastUpdate();
                holdOffRelease = start + holdOffPeriod;
                logger.debug("Condition satisfied, holding the result for " + holdOffPeriod + "ms, release time: " + holdOffRelease);
            }

            if (daq.getLastUpdate() >= holdOffRelease) {
                logger.debug("Releaseing condition from " + holdOffPeriod + "ms hold off period");
                return true;
            }
        } else {
            start = null;
        }

        return false;
    }
}
