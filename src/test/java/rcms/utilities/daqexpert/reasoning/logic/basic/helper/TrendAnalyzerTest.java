package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class TrendAnalyzerTest {

    @Test
    public void testStrictIncreasing() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.STRICT);
        IntStream.rangeClosed(1, 10).map(v -> 2 * v).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.INCREASING, trendAnalyzer.getTrend());
    }

    @Test
    public void testStrictDecreasing() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.STRICT);
        IntStream.rangeClosed(1, 10).map(v -> 100 - v).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.DECREASING, trendAnalyzer.getTrend());
    }


    @Test
    public void testStrictStable() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.STRICT);
        IntStream.of(2, 2, 2, 2, 2, 2, 2, 2).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.STABLE, trendAnalyzer.getTrend());
    }

    @Test
    public void testNonStrictStable() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.NON_STRICT);
        IntStream.of(10, 10, 10, 11, 10, 9, 10, 10).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.STABLE, trendAnalyzer.getTrend());
    }

    @Test
    public void testNonStrictStable1() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.NON_STRICT);
        IntStream.of(10, 10, 10, 11, 9, 10, 10).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.STABLE, trendAnalyzer.getTrend());
    }

    @Test
    public void testNonStrictStable2() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.NON_STRICT);
        IntStream.of(10, 8, 8, 7, 8, 7, 7).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.STABLE, trendAnalyzer.getTrend());
    }

    @Test
    public void testVarying() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.STRICT);
        IntStream.of(1, 2, 1, 2, 1, 3, 1).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.VARYING, trendAnalyzer.getTrend());
    }

    @Test
    public void testRandom() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.STRICT);
        IntStream.of(1, 4, 5, 6, 6, 8, 7).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.INCREASING, trendAnalyzer.getTrend());
    }

    @Test
    public void testNotEstablished() {
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.STRICT);
        IntStream.of(1, 4).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.NOT_ESTABLISHED, trendAnalyzer.getTrend());
    }

    /**
     * Real life test - ram disk usage should be considered decreasing here http://daq-expert
     * .cms/DAQExpert/?start=2018-10-22T21:55:21.932Z&end=2018-10-22T21:57:26.602Z
     */
    @Test
    public void testRealData() {
        Logger.getLogger(TrendAnalyzer.class).setLevel(Level.TRACE);
        TrendAnalyzer trendAnalyzer = new TrendAnalyzer(TrendConfiguration.builder().delta(0.5).trendEstablishCount(10)
                                                                .build());
        DoubleStream.of(64.30113220214844, 63.722511291503906, 63.13095474243164,
                        62.4553337097168, 61.834232330322266, 60.573123931884766,
                        59.96526336669922, 59.45234680175781, 58.81793975830078,
                        58.29673767089844, 57.58876419067383, 56.875633239746094
        ).forEach(c -> trendAnalyzer.update(c));
        trendAnalyzer.calculate();
        Assert.assertEquals(Trend.DECREASING, trendAnalyzer.getTrend());
    }

}