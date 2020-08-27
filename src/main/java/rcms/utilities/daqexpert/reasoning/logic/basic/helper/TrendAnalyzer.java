package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

/**
 * 1. Values are collected in the circular buffer of fixed capacity - according to configuration. If accumulated delta
 * between updates is higher than threshold (see configuration) and vast majority of deltas were positive - there is
 * increasing trend. If accumulated delta between updates is lower than negated threshold and vast majority of deltas
 * were negative - there is decreasing trend. If absolute value of accumulated deltas is lower than threshold - there is
 * stable trend. Otherwise it's varying trend
 */
public class TrendAnalyzer {


    private CircularFifoQueue<Float> queue;

    private Trend currentTrend;

    private TrendConfiguration configuration;

    public static final Logger logger = Logger.getLogger(TrendAnalyzer.class);

    public Trend getTrend() {
        return currentTrend;
    }

    public TrendAnalyzer(TrendConfiguration configuration) {
        this.configuration = configuration;
        this.queue = new CircularFifoQueue<>(configuration.getTrendEstablishCount());

    }

    public TrendAnalyzer update(Long value) {
        update(value.floatValue());
        return this;
    }

    public TrendAnalyzer update(Integer value) {
        update(value.floatValue());
        return this;
    }

    public TrendAnalyzer update(Double value) {
        update(value.floatValue());
        return this;
    }

    public TrendAnalyzer update(Float value) {
        logger.debug("Current value: " + value);
        queue.add(value);
        return this;
    }

    public TrendAnalyzer calculate() {


        if (queue.size() < configuration.getTrendEstablishCount()) {
            this.currentTrend = Trend.NOT_ESTABLISHED;
            return this;
        }

        float accumulatedDelta = 0.0f;

        int increasing = 0, decreasing = 0;

        Float previous = null;
        for (Float current : queue) {
            if (previous != null && current != null) {

                Float delta = current - previous;

                accumulatedDelta += delta;

                if (delta > 0) {
                    increasing++;
                } else if (delta < 0) {
                    decreasing++;
                }

                logger.trace("delta: " + delta + " between: " + previous + " and " + current);
            }
            previous = current;
        }

        // detect varying - incrasing and decreasing similar
        int numberOfComparisons = queue.size() - 1;
        float incrasingFraction = 1.0f * increasing / numberOfComparisons;
        float decreasingFraction = 1.0f * decreasing / numberOfComparisons;

        boolean possiblyVarying = false;
        if (incrasingFraction > 0.25 && decreasingFraction > 0.25) {
            possiblyVarying = true;
        }
        logger.trace("Fraction of increasing/decreasing: " + incrasingFraction + "/" + decreasingFraction);
        logger.trace("Accumulated delta: " + accumulatedDelta);
        logger.trace("Possibly varying: " + possiblyVarying);


        if (Math.abs(accumulatedDelta) <= configuration.getDelta()) {
            this.currentTrend = Trend.STABLE;
        } else if (accumulatedDelta > configuration.getDelta() && !possiblyVarying) {
            this.currentTrend = Trend.INCREASING;
        } else if (-accumulatedDelta > configuration.getDelta() && !possiblyVarying) {
            this.currentTrend = Trend.DECREASING;
        } else {
            this.currentTrend = Trend.VARYING;
        }
        logger.trace("Result: " + currentTrend);

        return this;
    }

}
