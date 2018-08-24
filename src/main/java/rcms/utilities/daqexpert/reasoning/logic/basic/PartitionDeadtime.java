package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
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

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.ExpectedRate);

        declareAffected(LogicModuleRegistry.TTSDeadtime);
    }

    /**
     * Dead time when greater than 5%
     */
    @Override
    public boolean satisfied(DAQ daq) {

        boolean expectedRate = false;
        expectedRate = getOutputOf(LogicModuleRegistry.ExpectedRate).getResult();
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
                    contextHandler.registerObject("PROBLEM-PARTITION", partition, p->p.getName());
                    contextHandler.registerObject("PROBLEM-SUBSYSTEM", partition.getSubsystem(), s->s.getName());
                    contextHandler.registerForStatistics("DEADTIME", deadPercentage, "%", 1);
                }
            }
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_PARTITION, this.getClass());
        this.description = "Deadtime of partition(s) {{PROBLEM-PARTITION}} in subsystem(s) {{PROBLEM-SUBSYSTEM}} is {{DEADTIME}} the threshold is "
                + threshold + "%";
        this.briefDescription = "Deadtime of partition(s) {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-PARTITION}} is {{DEADTIME}}";

    }

}
