package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

public class TrendAnalyzer {


    private CircularFifoQueue<Float> queue;

    private Trend currentTrend;

    private TrendConfiguration configuration;

    public static final Logger logger = Logger.getLogger(TrendAnalyzer.class);

    public Trend getTrend() {
        return currentTrend;
    }

    public TrendAnalyzer(TrendConfiguration configuration){
        this.configuration = configuration;
        this.queue = new CircularFifoQueue<>(configuration.getTrendEstablishCount() * 2);

    }

    public TrendAnalyzer update(Long value){
        queue.add(value.floatValue());
        return this;
    }

    public TrendAnalyzer update(Integer value){
        queue.add(value.floatValue());
        return this;
    }

    public TrendAnalyzer update(Double value) {
        queue.add(value.floatValue());
        return this;
    }

    public TrendAnalyzer update(Float value) {
        queue.add(value);
        return this;
    }

    public TrendAnalyzer calculate() {

        if(queue.size() < configuration.getTrendEstablishCount()){
            this.currentTrend = Trend.NOT_ESTABLISHED;
            return this;
        }

        int increasing = 0, decreasing = 0, same = 0;

        Float previous = null;
        for(Float current : queue){
            if(previous != null && current != null){

                Float delta = current - previous;

                if(Math.abs(delta) <= configuration.getDelta()){
                    same++;
                } else if(delta > configuration.getDelta()){
                    increasing++;
                } else if(-delta > configuration.getDelta()){
                    decreasing++;
                }


                logger.trace("delta: " + delta + " between: " + previous + " and " + current);
            }
            previous= current;
        }
        logger.debug("Same: " + same);
        logger.debug("Increasing: " + increasing);
        logger.debug("Decreasing: " + decreasing);

        if(same >= configuration.getTrendEstablishCount()){
            this.currentTrend = Trend.STABLE;
        }else if(increasing >= configuration.getTrendEstablishCount()){
            this.currentTrend = Trend.INCREASING;
        } else if(decreasing >= configuration.getTrendEstablishCount()){
            this.currentTrend = Trend.DECREASING;
        }else {
            this.currentTrend = Trend.VARYING;
        }

        return this;
    }

}
