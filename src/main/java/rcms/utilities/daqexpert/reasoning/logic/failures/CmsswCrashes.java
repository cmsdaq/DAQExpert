package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Logic module identifying CMSSW crashes
 */
public class CmsswCrashes extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(CmsswCrashes.class);

    private int crashesCountThreshold;
    private int slidingWindowPeriodInSeconds;

    /* Time window of previous data and previous results. Contains timestamp, monitored value, and output of LM */
    private List<Triple<Long, Integer, Boolean>> timeWindow;

    private String additionalNote = "Note that there is also backpressure from HLT.";

    public CmsswCrashes() {
        timeWindow = new LinkedList<>();
        this.name = "CMSSW crashes";


        this.briefDescription = "CMSSW crashes frequently, there are {{CRASHES}}";
        this.action = new SimpleAction("Call the HLT DOC, mentioning the messages under you see under HLT Alerts in F3 Mon. ",
                "Call the DAQ DOC. He might need to clean up the Filter Farm.");


    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.BackpressureFromHlt);
        declareAffected(LogicModuleRegistry.BackpressureFromHlt);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        assignPriority(results);

        if(daq.getHltInfo() == null){
            return false;
        }

        int currentCrashes = daq.getHltInfo().getCrashes();
        long startTimestampOfSlidingWindow = daq.getLastUpdate() - slidingWindowPeriodInSeconds * 1000;

        Triple<Long, Integer, Boolean> reference = timeWindow.stream().filter(e -> e.getLeft() >= startTimestampOfSlidingWindow).findFirst().orElse(null);

        boolean result = false;
        if (reference != null) {

            int currentCrashIncrement = currentCrashes - reference.getMiddle();
            long currentTimeIncrement = daq.getLastUpdate() - reference.getLeft();

            logger.debug("Current time window: " + timeWindow + " reference element: " + reference + ", current increment: " + currentCrashIncrement);

            if (crashesCountThreshold <= currentCrashIncrement) {
                float crashesPerSecond = currentCrashIncrement / (0.001f*currentTimeIncrement);
                contextHandler.registerForStatistics("CRASHES", crashesPerSecond, " crashes/s", 1);
                result = true;
            }

        }

        timeWindow.add(Triple.of(daq.getLastUpdate(), currentCrashes, result));

        // update the sliding window - keep as less data as possible for next iteration
        timeWindow = timeWindow.stream().filter(e -> e.getLeft() >= startTimestampOfSlidingWindow).collect(Collectors.toList());

        return result;


    }

    @Override
    public void parametrize(Properties properties) {
        this.crashesCountThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_CMSSW_CRASHES_THRESHOLD, this.getClass());
        this.slidingWindowPeriodInSeconds = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_CMSSW_CRASHES_TIME_WINDOW, this.getClass());

        this.description = "CMSSW crashes frequently, there are {{CRASHES}}, which exceeds the threshold of " + crashesCountThreshold + " crashes per " + slidingWindowPeriodInSeconds + "s.";

        logger.debug("Parametrized: " + description);
    }

}
