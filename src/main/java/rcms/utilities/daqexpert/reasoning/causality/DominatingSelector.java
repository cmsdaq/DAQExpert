package rcms.utilities.daqexpert.reasoning.causality;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.processing.Requiring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        if(conditions.size() > 1){
            logger.info("Requirement graph will be applied to select dominating condition");
        } else{
            return conditions.iterator().next();
        }

        Set<Condition> subResult1 = getLeafsFromUsageGraph(conditions);

        if(subResult1.size() > 1){
            logger.info("Causality graph will be applied to select dominating condition");
        } else{
            return subResult1.iterator().next();
        }

        Set<Condition> subResult2 = getLeafsFromCausality(subResult1);

        if(subResult2.size() > 1){
            logger.info("Scoring will be applied to select dominating condition");
        } else{
            return subResult2.iterator().next();
        }

        logger.warn("Could not find dominating condition "+ subResult2);
        logger.warn("Selecting the first "+ subResult2.iterator().next());
        return subResult2.iterator().next();

    }

    public Set<Condition> getLeafsFromUsageGraph(Set<Condition> conditions) {


        logger.info("Initial set of conditions (before usage graph):" );
        conditions.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);

        // choose nodes that are not required by anything
        Set<Requiring> requiredByOther = new HashSet<>();

        for (Condition condition : conditions) {
            requiredByOther.addAll(condition.getLogicModule().getLogicModule().getRequired());
        }

        Set<Condition> filtered = conditions.stream().filter(c -> !requiredByOther.contains(c.getLogicModule().getLogicModule())).collect(Collectors.toSet());


        logger.info("Filtered set of conditions (after usage graph):" );
        filtered.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);

        return filtered;

    }

    public Set<Condition> getLeafsFromCausality(Set<Condition> conditions){

        logger.info("Initial (before causality graph):" );
        conditions.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);


        Set<CausalityNode> causedByOther = new HashSet<>();

        for(Condition condition: conditions){
            causedByOther.addAll(condition.getLogicModule().getLogicModule().getCausing());
        }

        Set<Condition> filtered = conditions.stream().filter(c -> causedByOther.contains(c.getLogicModule().getLogicModule())).collect(Collectors.toSet());

        logger.info("Filtered (after causality graph):" );
        filtered.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);


        return filtered;
    }
}
