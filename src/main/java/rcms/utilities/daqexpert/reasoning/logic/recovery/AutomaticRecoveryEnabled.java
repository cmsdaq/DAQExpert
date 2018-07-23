package rcms.utilities.daqexpert.reasoning.logic.recovery;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.Requiring;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

import java.util.Map;

public class AutomaticRecoveryEnabled extends SimpleLogicModule {


    public AutomaticRecoveryEnabled(){
        this.name = "Automatic recovery enabled";
        this.priority = ConditionPriority.DEFAULTT;
        this.problematic = false;
    }

    @Override
    public void declareRelations() {
        require(LogicModuleRegistry.DataflowEstablished);
        require(LogicModuleRegistry.ProblemEstablished);
        require(LogicModuleRegistry.L0AutomaticRecoveryAction);
        require(LogicModuleRegistry.NoRateWhenExpected);
    }


    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean dataflowEstablished = results.get(DataflowEstablished.class.getSimpleName()).getResult();
        boolean problemEstablished = results.get(ProblemEstablished.class.getSimpleName()).getResult();
        boolean l0recovering= results.get(L0AutomaticRecovery.class.getSimpleName()).getResult();
        boolean dataflowStuck = results.get(NoRateWhenExpected.class.getSimpleName()).getResult();

        if(dataflowStuck && dataflowEstablished && problemEstablished && !l0recovering){
            return true;
        }

        return false;
    }


}
