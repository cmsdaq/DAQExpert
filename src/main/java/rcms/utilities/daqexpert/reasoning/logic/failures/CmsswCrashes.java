package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.Map;
import java.util.Properties;

/**
 * Logic module identifying CMSSW crashes
 */
public class CmsswCrashes extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(CmsswCrashes.class);

    private int threshold;

    public CmsswCrashes() {
        this.name = "CMSSW crashes";


        this.action = new SimpleAction("Call the HLT DOC, mentioning the messages under you see under HLT Alerts in F3 Mon.",
                " Call the DAQ DOC. He might need to clean up the Filter Farm.");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
        int currentCrashes = daq.getHltInfo().getCrashes();

        // assign the priority based on whether we are in stable beams or not
        assignPriority(results);

        logger.trace("Current HLT output bandwidth is: " + currentCrashes);

        boolean result = false;
        if (threshold < currentCrashes) {
            context.registerForStatistics("CRASHES", currentCrashes, "", 1);
            result = true;
        }
        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "CMSSW crashes " + threshold + " ";

        logger.debug("Parametrized: " + description);
    }

}
