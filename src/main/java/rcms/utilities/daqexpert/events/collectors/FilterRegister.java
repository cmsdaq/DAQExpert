package rcms.utilities.daqexpert.events.collectors;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

public abstract class FilterRegister implements EventRegister {

    protected boolean isImportant(Condition condition) {
        LogicModuleRegistry logicModule = condition.getLogicModule();
        if (logicModule == null) {
            throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Condition has no logic module assigned");
        }
        if (condition.isShow()) {
            if (logicModule.getLogicModule() instanceof SimpleLogicModule) {
                if (logicModule.getLogicModule() instanceof ContextLogicModule) {
                    if (!condition.isHoldNotifications()) {
                        return true;
                    } else return false;
                } else if (condition.getPriority().ordinal() > ConditionPriority.DEFAULTT.ordinal()) {
                    return true;
                } else {
                    return false;
                }
            } else if (logicModule.getLogicModule() instanceof ComparatorLogicModule) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

}
