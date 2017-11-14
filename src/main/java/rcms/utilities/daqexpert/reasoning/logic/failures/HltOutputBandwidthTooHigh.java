package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.util.Map;
import java.util.Properties;

public class HltOutputBandwidthTooHigh extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(HltOutputBandwidthTooHigh.class);

    private String additionalNote = "Note that there is also backpressure from HLT.";

    /**
     * upper end of range for expected  rate
     */
    private double bandwidthThresholdInGbps;
    public HltOutputBandwidthTooHigh() {
        this.name = "Too high HLT output bandwidth";
        this.bandwidthThresholdInGbps = 0;

        this.action = new SimpleAction("Talk to the trigger shifter and shift leader. " +
                "Have them check the pre-scale column. You may need to call the HLT DOC.");
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        // assign the priority based on whether we are in stable beams or not
        assignPriority(results);

        double currentOutputBandwidthInGbps = daq.getBuSummary().getFuOutputBandwidthInMB() / 1024;
        logger.trace("Current HLT output bandwidth is: " + currentOutputBandwidthInGbps);

        boolean result = false;
        if (bandwidthThresholdInGbps < currentOutputBandwidthInGbps) {
            context.registerForStatistics("BANDWIDTH", currentOutputBandwidthInGbps, "GB/s", 1);
            result = true;
        }

        if (results.get(BackpressureFromHlt.class.getSimpleName())) {
            //mention the fact that some modules are active
            context.registerConditionalNote("NOTE", additionalNote);
        } else{
            context.unregisterConditionalNote("NOTE");
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        try {
            this.bandwidthThresholdInGbps = Double.parseDouble(properties.getProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey()));
            this.description = "The HLT output bandwidth is {{BANDWIDTH}} which is above the threshold of "
                    + bandwidthThresholdInGbps + " GB/s at which delays Rate Monitoring and Express streams can appear. " +
                    "DQM files may get truncated resulting in lower statistics. [[NOTE]]";

            logger.debug("Parametrized: " + description);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
        }
    }

}