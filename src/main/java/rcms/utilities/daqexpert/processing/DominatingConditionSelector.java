package rcms.utilities.daqexpert.processing;

import rcms.utilities.daqexpert.persistence.Condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DominatingConditionSelector {

    public static Condition findDominating(Condition dominatinCondition, Condition condition){

        return findDominating(dominatinCondition, Arrays.asList(condition));
    }

    public static Condition findDominating(List<Condition> conditions){

        return findDominating(null, conditions);
    }

    public static Condition findDominating(Condition dominatingCondition, List<Condition> conditions) {

        if(conditions.size() == 0){
            return null;
        }

        for(Condition condition: conditions) {

            // exists some unfinished
            if (condition.getEnd() == null) {

                // no condition at the moment
                if (dominatingCondition == null) {
                    dominatingCondition = condition;
                }

                // exists other condition at the moemnt
                else {

                    // current is more important than old
                    if (condition.getPriority().ordinal() > dominatingCondition.getPriority().ordinal()) {
                        dominatingCondition = condition;
                    }

                    // current is less important than old
                    else if (condition.getPriority().ordinal() < dominatingCondition.getPriority().ordinal()) {
                        // nothing to do
                    }

                    // both are equally important
                    else {

                        // current is more useful than old
                        if (condition.getLogicModule().getUsefulness() > dominatingCondition.getLogicModule()
                                .getUsefulness()) {
                            dominatingCondition = condition;

                        }
                        // current is less useful than old
                        else if (condition.getLogicModule().getUsefulness() < dominatingCondition.getLogicModule()
                                .getUsefulness()) {
                            // nothing to do
                        }
                        // both are equally useful
                        else {
                            // newest will be displayed
                            if (condition.getStart().after(dominatingCondition.getStart())) {
                                dominatingCondition = condition;
                            }
                        }

                    }
                }
            }
        }

        return dominatingCondition;

    }
}
