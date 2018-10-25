package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TrendConfiguration {


    /**
     * Minimum count of values to establish trend.
     */
    private final Integer trendEstablishCount;
    /**
     * n - value can change in percent 0 - strict, same values will hold the trend -1 - strict, same values will
     * break
     * the trend
     */
    private final Double delta;

    public static TrendConfiguration STRICT = TrendConfiguration.builder().trendEstablishCount(4).delta(0.0).build();

    public static TrendConfiguration NON_STRICT = TrendConfiguration.builder().trendEstablishCount(4).delta(1.0).build();
}
