package rcms.utilities.daqexpert.reasoning.logic.recovery;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.Map;
import java.util.Properties;

public class ProblemEstablished extends SimpleLogicModule implements Parameterizable {

    private Long dataflowStuckStart;
    private Long threshold;

    public ProblemEstablished() {
        this.name = "Problem established";
        this.priority = ConditionPriority.DEFAULTT;
        this.problematic = false;
    }

    @Override
    public void declareRelations() {
        require(LogicModuleRegistry.NoRateWhenExpected);
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean result = false;

        if (results.get(NoRateWhenExpected.class.getSimpleName()).getResult() == true) {
            if (dataflowStuckStart == null) {
                dataflowStuckStart = daq.getLastUpdate();
            }
        } else {
            dataflowStuckStart = null;
            return false;
        }

        if (dataflowStuckStart + threshold < daq.getLastUpdate()) {
            result = true;
        }

        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.PROBLEM_ESTABLISHED, this.getClass()).longValue();
        this.description = "Dataflow problem lasts for more than " + (threshold / 1000) + "s";
    }
}
