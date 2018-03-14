package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.util.Map;
import java.util.Properties;

public class HltOutputBandwidthExtreme extends KnownFailure implements Parameterizable {

    private double bandwidthThresholdInGbps;
    private String additionalNote = "Note that there is also backpressure from HLT.";

    public HltOutputBandwidthExtreme() {
        this.name = "Extreme HLT output bandwidth";
        this.bandwidthThresholdInGbps = 0;

        this.action = new SimpleAction( "Talk to the trigger shifter and shift leader. Have them check the pre-scale column. ",
                "Check the per-stream bandwidths in F3Mon. You may need to call the HLT DOC."
        );
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        // assign the priority based on whether we are in stable beams or not
        assignPriority(results);

        double currentOutputBandwidthInGbps = daq.getBuSummary().getFuOutputBandwidthInMB() / 1024;

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
            this.bandwidthThresholdInGbps = Double.parseDouble(properties.getProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey()));
            this.description = "The HLT output bandwidth is {{BANDWIDTH}} which is above the expected maximum " + bandwidthThresholdInGbps + " GB/s. " +
                    "You should not continue running in these conditions. " +
                    "Otherwise you risk problems with the NFS mounts on the FUs which can take a long time to recover. " +
                    "[[NOTE]]";

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
        }
    }

}
