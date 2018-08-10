package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.util.Properties;

public class HltOutputBandwidthTooHigh extends HltOutputBandwidthExceeded {

    private String additionalNote = "Note that there is also backpressure from HLT.";

    public HltOutputBandwidthTooHigh() {
        super(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH);
        this.name = "Too high HLT output bandwidth";

        this.briefDescription = "The HLT output bandwidth is high: {{BANDWIDTH}}";
        this.action = new SimpleAction("Talk to the trigger shifter and shift leader. " +
                "Have them check the pre-scale column. You may need to call the HLT DOC.");
    }

    @Override
    public void parametrize(Properties properties) {

        super.parametrize(properties);

        // TODO: even if bandwidthThresholdInGbps is null we should not
        //       get a NullPointerException here
        try {
            this.description = "The HLT output bandwidth is {{BANDWIDTH}} which is above the threshold of "
                    + bandwidthThresholdInGbps + " GB/s at which delays to Rate Monitoring and Express streams can appear. " +
                    "DQM files may get truncated resulting in lower statistics. This mode of operation may be normal for special runs if experts are monitoring.";

        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
        }
    }

}
