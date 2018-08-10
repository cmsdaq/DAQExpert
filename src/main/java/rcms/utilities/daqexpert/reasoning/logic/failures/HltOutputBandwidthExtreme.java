package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;

import java.util.Map;
import java.util.Properties;

public class HltOutputBandwidthExtreme extends HltOutputBandwidthExceeded {

    private String additionalNote = "Note that there is also backpressure from HLT.";

    public HltOutputBandwidthExtreme() {
        super(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME);
        this.name = "Extreme HLT output bandwidth";

        this.briefDescription = "The HLT output bandwidth is extreme: {{BANDWIDTH}}";
        this.action = new SimpleAction( "Talk to the trigger shifter and shift leader. Have them check the pre-scale column. ",
                "Check the per-stream bandwidths in F3Mon. You may need to call the HLT DOC."
        );
    }

    @Override
    public void declareRelations(){
        super.declareRelations();
        require(LogicModuleRegistry.HltOutputBandwidthTooHigh);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        // do not fire if the (lower threshold) warning does not fire
        // (for consistency reasons)
        if (!results.get(HltOutputBandwidthTooHigh.class.getSimpleName()).getResult()) {
            return false;
        }

	return super.satisfied(daq, results);
    }

    @Override
    public void parametrize(Properties properties) {

        super.parametrize(properties);

        // TODO: even if bandwidthThresholdInGbps is null we should not
        //       get a NullPointerException here
        try {
            this.description = "The HLT output bandwidth is {{BANDWIDTH}} which is above the expected maximum " + bandwidthThresholdInGbps + " GB/s. " +
                    "You should not continue running in these conditions. " +
                    "Otherwise you risk problems with the NFS mounts on the FUs which can take a long time to recover. ";

        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
        }
    }

}
