package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.Map;
import java.util.Properties;

public class HltOutputBandwidthExtreme extends KnownFailure implements Parameterizable {

    /**
     * upper end of range for expected  rate
     */
    private double max;

    public HltOutputBandwidthExtreme() {
        this.name = "Extreme HLT output bandwidth";
        this.max = 0;

        this.action = new SimpleAction("Are we running with the correct pre-scale column?",
                "Talk to the trigger shifter and shift leader",
                "You may need to call the HLT DOC",
                "Check if DAQ backpressure from Filter Farm also fired and mention if it did. (This may appear after the condition has started)"
        );
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        // assign the priority based on whether we are in stable beams or not
        assignPriority(results);

        double outputBandwidth = daq.getBuSummary().getFuOutputBandwidthInMB();

        boolean result = false;
        if (max < outputBandwidth) {
            context.registerForStatistics("BANDWIDTH", outputBandwidth, "MB/s", 1);
            result = true;
        }
        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        try {
            this.max = Double.parseDouble(properties.getProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey()));
            this.description = "The HLT output bandwidth is {{BANDWIDTH}} which is above the expected maximum " + max + " MB/s";

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
        }
    }

}
