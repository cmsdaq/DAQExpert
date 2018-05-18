package rcms.utilities.daqexpert.reasoning.causality;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.Requiring;
import rcms.utilities.daqexpert.reasoning.base.ComparatorLogicModule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects the most important condition from the set of conditions that are satisfied at given moment. It follows the
 * strategy: 1. if conditions belong to the same "usage" graph it will select the leaf 2. otherwise if conditions belong
 * to the same "casuality" graph it will select the leaf 3. otherwise if conditions are not related it will select the
 * one with the highest number of context entries.
 */
public class DominatingSelector {

    private static Logger logger = Logger.getLogger(DominatingSelector.class);

    public Condition selectDominating(Set<Condition> conditions) {

        if(conditions.size() == 0){
            throw new ExpertException(ExpertExceptionCode.ExpertProblem,"Bad arguments for dominating selector, 0 conditions supplied");
        }

        // ignore conditions that has end date
        Set<Condition> filtered = conditions.stream().filter(c->c.getEnd() == null).collect(Collectors.toSet());
        if(filtered.size() == 1) {
            return filtered.iterator().next();
        } else if(filtered.size() == 0){
            logger.info("No dominating has been selected (after filtering unfinished conditions nothing left)");
            return null;
        }

        // ignore conditions that are comparators
        filtered = filtered.stream().filter(c->!(c.getLogicModule().getLogicModule() instanceof ComparatorLogicModule)).collect(Collectors.toSet());

        logger.debug("Requirement graph will be applied to select dominating condition");
        Set<Condition> subResult1 = getLeafsFromUsageGraph(filtered);
        if(subResult1.size() == 1) {
            return subResult1.iterator().next();
        } else if(subResult1.size() == 0){
            logger.info("No dominating has been selected (after applying requirement graph nothing left)");
            return null;
        }

        logger.debug("Causality graph will be applied to select dominating condition");
        Set<Condition> subResult2 = getLeafsFromCausality(subResult1);
        if(subResult2.size() == 1) {
            return subResult2.iterator().next();
        } else if(subResult2.size() == 0){
            logger.info("No dominating has been selected (after applying causality graph nothing left)");
            return null;
        }

        logger.debug("Start date will be applied to select dominating condition");
        Date earliest = null;
        for(Condition condition: subResult2){
            if(earliest == null) {
                earliest = condition.getStart();
            }
            if(earliest.getTime() > condition.getStart().getTime()){
                earliest = condition.getStart();
            }
        }
        final Date earliestFinal = earliest;

        Set<Condition> subResult3 = conditions.stream().filter(c-> earliestFinal.equals(c.getStart())).collect(Collectors.toSet());

        if(subResult3.size() == 1) {
            return subResult3.iterator().next();
        } else if(subResult3.size() == 0){
            logger.info("No dominating has been selected (after soring based on start date nothing left)");
            return null;
        }

        logger.warn("Could not find dominating condition "+ subResult3);
        logger.warn("Selecting the first randomly "+ subResult3.iterator().next());
        return subResult3.iterator().next();

    }

    public Set<Condition> getLeafsFromUsageGraph(Set<Condition> conditions) {


        logger.debug("Initial set of conditions (before usage graph):" + conditions);
        conditions.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::debug);

        // choose nodes that are not required by anything
        Set<Requiring> requiredByOther = new HashSet<>();

        for (Condition condition : conditions) {
            requiredByOther.addAll(condition.getLogicModule().getLogicModule().getRequired());
        }

        Set<Condition> filtered = conditions.stream().filter(c -> !requiredByOther.contains(c.getLogicModule().getLogicModule())).collect(Collectors.toSet());


        logger.debug("Filtered set of conditions (after usage graph):" );
        filtered.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::debug);

        return filtered;

    }



    public Set<Condition> getLeafsFromCausality(Set<Condition> conditions){

        logger.debug("Initial (before causality graph):" );
        conditions.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::debug);


        Map<Condition, Set<Condition>> forbidden = new HashMap<>();

        // O(n^2) computation complexity. Reasonable taking int account that this n is in order of 1-10
        for(Condition condition: conditions){
            for(Condition condition1: conditions) {
                /* Do not compare to self */
                if(!condition1.equals(condition)) {
                    if (condition.getLogicModule().getLogicModule().getAffected().contains(condition1.getLogicModule().getLogicModule())) {

                        if(!forbidden.containsKey(condition1)){
                            forbidden.put(condition1, new HashSet<>());
                        }

                        forbidden.get(condition1).add(condition);
                    }
                }

            }
        }

        Set<Condition> result = new HashSet<>();

        logger.debug("Forbidden map (after causality graph):" );
        forbidden.entrySet().stream().map(e->e.getKey().getLogicModule().name() + " by " + e.getValue().stream().map(c->c.getLogicModule().name()).collect(Collectors.toSet())).forEach(logger::debug);


        for(Condition condition: conditions){
            if(!forbidden.containsKey(condition)){
                result.add(condition);
            }
        }

        logger.debug("Filtered (after causality graph):" );
        result.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::debug);

        return result;
    }
}
