package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Logic module identifying CMSSW crashes
 */
public class CmsswCrashes extends KnownFailure implements Parameterizable {

    private static final Logger logger = Logger.getLogger(CmsswCrashes.class);

    private int crashesCountThreshold;
    private int slidingWindowPeriodInSeconds;
    private int holdoffPeriodInSeconds;

    /* Time window of previous data and previous results. Contains timestamp, monitored value, and output of LM */
    private List<Triple<Long, Integer, Boolean>> timeWindow;

    public CmsswCrashes() {
        timeWindow = new LinkedList<>();
        this.name = "CMSSW crashes";


        this.action = new SimpleAction("Call the HLT DOC, mentioning the messages under you see under HLT Alerts in F3 Mon. ",
                "Call the DAQ DOC. He might need to clean up the Filter Farm.");

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        assignPriority(results);

        int currentCrashes = daq.getHltInfo().getCrashes();
        long startTimestampOfSlidingWindow = daq.getLastUpdate() - slidingWindowPeriodInSeconds * 1000;
        long holdoffLimit = daq.getLastUpdate() - holdoffPeriodInSeconds * 1000;

        Triple<Long, Integer, Boolean> reference = timeWindow.stream().filter(e -> e.getLeft() >= startTimestampOfSlidingWindow).findFirst().orElse(null);

        boolean result = false;
        if (reference != null) {

            int currentIncrement = currentCrashes - reference.getMiddle();

            logger.debug("Current time window: " + timeWindow + " reference element: " + reference + ", current increment: " + currentIncrement);

            if (crashesCountThreshold <= currentIncrement) {
                context.registerForStatistics("CRASHES", currentCrashes, "", 1);
                result = true;
            }

        }

        timeWindow.add(Triple.of(daq.getLastUpdate(), currentCrashes, result));

        // update the sliding window - keep as less data as possible for next iteration
        timeWindow = timeWindow.stream().filter(e -> e.getLeft() >= Math.min(startTimestampOfSlidingWindow, holdoffLimit)).collect(Collectors.toList());

        if (!result) {
            logger.info("Filtered: " + timeWindow.stream().filter(e -> e.getLeft() > holdoffLimit).collect(Collectors.toList()));
            return timeWindow.stream().filter(e -> e.getLeft() > holdoffLimit).anyMatch(e -> e.getRight());
        } else
            return true;


    }

    @Override
    public void parametrize(Properties properties) {
        this.crashesCountThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_CMSSW_CRASHES_THRESHOLD, this.getClass());
        this.slidingWindowPeriodInSeconds = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_CMSSW_CRASHES_TIME_WINDOW, this.getClass());
        this.holdoffPeriodInSeconds = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_CMSSW_CRASHES_HOLDOFF, this.getClass());

        this.description = "CMSSW crashes " + crashesCountThreshold + " ";

        logger.debug("Parametrized: " + description);
    }

}
