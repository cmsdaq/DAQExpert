package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies partition deadtime
 */
public class PartitionDeadtime extends ContextLogicModule implements Parameterizable {

    private float threshold;

    public PartitionDeadtime() {
        this.name = "Partition deadtime";
        this.priority = ConditionPriority.DEFAULTT;
        this.threshold = 0;
    }

    /**
     * Dead time when greater than 5%
     */
    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean expectedRate = false;
        expectedRate = results.get(ExpectedRate.class.getSimpleName());
        if (!expectedRate)
            return false;

        boolean result = false;

        Iterator<TTCPartition> i = daq.getTtcPartitions().iterator();

        while (i.hasNext()) {
            TTCPartition partition = i.next();
            if (!partition.isMasked()) {
                float deadPercentage = 0;
                deadPercentage += partition.getPercentBusy();
                deadPercentage += partition.getPercentWarning();

                if (deadPercentage > threshold) {
                    result = true;
                    context.register("TTCP", partition.getName());
                    context.register("SUBSYSTEM", partition.getSubsystem().getName());
                    context.registerForStatistics("VALUE", deadPercentage, "%", 1);
                }
            }
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_PARTITION, this.getClass());
        this.description = "Deadtime of partition(s) {{TTCP}} in subsystem(s) {{SUBSYSTEM}} is {{VALUE}} the threshold is "
                + threshold + "%";

    }

}
