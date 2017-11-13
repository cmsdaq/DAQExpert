package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.Map;
import java.util.Properties;

public class FedGeneratesDeadtime extends KnownFailure implements Parameterizable {

    private float deadtimeThresholdInPercentage;
    private float backpressureThresholdInPercentage;

    public FedGeneratesDeadtime() {
        this.name = "FED problem";
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean fedDeadtime = results.get(FEDDeadtime.class.getSimpleName());
        if (!fedDeadtime) {
            return false;
        }
        boolean result = false;


        for (FED fed : daq.getFeds()) {
            float deadPercentage = 0;
            float backpressure = fed.getPercentBackpressure();
            deadPercentage += fed.getPercentBusy();
            deadPercentage += fed.getPercentWarning();

            if (deadPercentage > deadtimeThresholdInPercentage && backpressure < backpressureThresholdInPercentage) {
                result = true;
                context.register("FED",fed.getSrcIdExpected());
                context.registerForStatistics("DEADTIME",deadPercentage,"%",1);
                context.registerForStatistics("BACKPRESSURE",deadPercentage,"%",1);

            }

        }
        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.deadtimeThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
        this.backpressureThresholdInPercentage = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED, this.getClass());
        this.description = "FED {{FED}} generates a deadtime {{DEADTIME}}, the threshold is " + deadtimeThresholdInPercentage + "%";
    }

}
