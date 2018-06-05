package rcms.utilities.daqexpert.reasoning.logic.basic;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

import java.util.Map;
import java.util.Properties;

public class RateOutOfRange extends ContextLogicModule implements Parameterizable {

    private float min;
    private float max;


    //TODO: update description to display the rate
    //TODO: set problematic flag to true
    public RateOutOfRange() {
        this.name = "Rate out of range";
        this.priority = ConditionPriority.DEFAULTT;
        this.min = 0;
        this.max = 0;
    }

    @Override
    public void declareRequired() {
        require(LogicModuleRegistry.StableBeams);
        require(LogicModuleRegistry.NoRate);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {
        float a = daq.getFedBuilderSummary().getRate();

        boolean stableBeams = results.get(StableBeams.class.getSimpleName()).getResult();
        boolean noRate = results.get(NoRate.class.getSimpleName()).getResult();

        if (!stableBeams || noRate) {
            return false;
        }
        boolean result = false;
        if (min > a || max < a) {
            result = true;
            contextHandler.registerForStatistics("RATE", a / 1000f, "kHz", 1);
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {

        this.min = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_L1_RATE_MIN, this.getClass());
        this.max = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_L1_RATE_MAX, this.getClass());
        this.description = "L1 rate {{RATE}} is out of expected range (" + min / 1000f + " - " + max / 1000f + " kHz)";

    }

}
