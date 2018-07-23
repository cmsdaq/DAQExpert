package rcms.utilities.daqexpert.reasoning.logic.recovery;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.Map;
import java.util.Properties;

public class DataflowEstablished extends SimpleLogicModule implements Parameterizable {

    @Override
    public void declareRelations() {
        require(LogicModuleRegistry.NoRate);
    }

    public DataflowEstablished(){
        this.name = "Dataflow established";
        this.priority = ConditionPriority.DEFAULTT;
        this.problematic = false;
    }

    private Long dataflowStart;
    private Long startThreshold;
    private Long endThreshold;
    private Long lastEnd;
    private boolean previousResult;

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean result = false;

        boolean nonZeroRate = !results.get(NoRate.class.getSimpleName()).getResult();

        if(nonZeroRate){
            lastEnd = null;
            if(dataflowStart == null){
                dataflowStart = daq.getLastUpdate();
            }
        } else{
            if(lastEnd == null && dataflowStart != null) {
                lastEnd = daq.getLastUpdate();
            }
            dataflowStart = null;
        }

        if(nonZeroRate){
            if(dataflowStart + startThreshold < daq.getLastUpdate()){
                result = true;
            }
        } else{
            if(previousResult && lastEnd + endThreshold > daq.getLastUpdate()){
                result = true;
            }
        }
        previousResult = result;

        return result;
    }

    @Override
    public void parametrize(Properties properties) {
        this.startThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.DATAFLOW_ESTABLISHED, this.getClass()).longValue();
        this.endThreshold = FailFastParameterReader.getIntegerParameter(properties, Setting.DATAFLOW_ESTABLISHED, this.getClass()).longValue();
        this.description = "Dataflow lasts for more than "+(startThreshold /1000)+"s";
    }


}
