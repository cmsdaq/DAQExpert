package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
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

        this.briefDescription = "The HLT output bandwidth is high: {{BANDWIDTH}}";
        this.action = new SimpleAction("Talk to the trigger shifter and shift leader. " +
                "Have them check the pre-scale column. You may need to call the HLT DOC.");
    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.BackpressureFromHlt);
        declareAffected(LogicModuleRegistry.BackpressureFromHlt);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        // assign the priority based on whether we are in stable beams or not
        assignPriority(results);

        double currentOutputBandwidthInGbps = daq.getBuSummary().getFuOutputBandwidthInMB() / 1024;
        logger.trace("Current HLT output bandwidth is: " + currentOutputBandwidthInGbps);

        boolean result = false;
        if (bandwidthThresholdInGbps < currentOutputBandwidthInGbps) {
            contextHandler.registerForStatistics("BANDWIDTH", currentOutputBandwidthInGbps, "GB/s", 1);
            result = true;
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        try {
            this.bandwidthThresholdInGbps = Double.parseDouble(properties.getProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey()));
            this.description = "The HLT output bandwidth is {{BANDWIDTH}} which is above the threshold of "
                    + bandwidthThresholdInGbps + " GB/s at which delays to Rate Monitoring and Express streams can appear. " +
                    "DQM files may get truncated resulting in lower statistics. This mode of operation may be normal for special runs if experts are monitoring.";


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
