package rcms.utilities.daqexpert.reasoning.base;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ResultSupplier {

    private AccessController accessController;

    private HashMap<LogicModuleRegistry, Output> currentOutput;

    /**
     * If logic module wants to access past data
     */
    HashMap<LogicModuleRegistry, Queue<Output>> pastOutput;

    public ResultSupplier(){
        accessController = new AccessController();
        currentOutput = new HashMap<>();

        pastOutput = new HashMap<>();

        for(LogicModuleRegistry lmr: LogicModuleRegistry.values()){
            pastOutput.put(lmr, new CircularFifoQueue<>(3));
        }
    }

    public Output get(LogicModuleRegistry caller, LogicModuleRegistry called) {


        if(accessController.grantAccess(caller.getLogicModule(), called.getLogicModule() )){

            return currentOutput.get(called);
        } else{
            throw new ExpertException(ExpertExceptionCode.LogicModuleMisconfiguration, "LM " +caller.name()+ " requires results of a other LM "+called.name()+" that was never declared as required");
        }

    }

    public void clear() {
        for (Map.Entry<LogicModuleRegistry, Output> entry : currentOutput.entrySet()) {
            pastOutput.get(entry.getKey()).offer(entry.getValue());
        }
        currentOutput = new HashMap<>();
    }

    public void update(LogicModuleRegistry logicModuleRegistry, Output output) {
        currentOutput.put(logicModuleRegistry, output);
    }

    public Output get(LogicModuleRegistry key, int i) {
        //TODO: get i element of from the past ouptut
        return pastOutput.get(key).peek();
    }
}
