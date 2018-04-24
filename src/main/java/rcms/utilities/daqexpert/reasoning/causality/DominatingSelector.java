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

        return null;

    }

    public Set<Condition> getLeafsFromUsageGraph(Set<Condition> conditions) {


        logger.info("Initial:" );
        conditions.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);

        // choose nodes that are not required by anything
        Set<Requiring> requiredByOther = new HashSet<>();

        for (Condition condition : conditions) {
            requiredByOther.addAll(condition.getLogicModule().getLogicModule().getRequired());
        }

        Set<Condition> filtered = conditions.stream().filter(c -> !requiredByOther.contains(c.getLogicModule().getLogicModule())).collect(Collectors.toSet());


        logger.info("Filtered:" );
        filtered.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);

        return filtered;

    }

    public Set<Condition> getLeafsFromCausality(Set<Condition> conditions){

        logger.info("Initial:" );
        conditions.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);


        Set<Causing> causedByOther = new HashSet<>();

        for(Condition condition: conditions){
            causedByOther.addAll(condition.getLogicModule().getLogicModule().getCausing());
        }

        Set<Condition> filtered = conditions.stream().filter(c -> !causedByOther.contains(c.getLogicModule().getLogicModule())).collect(Collectors.toSet());

        logger.info("Filtered:" );
        filtered.stream().map(c->c.getLogicModule().getLogicModule().getName()).forEach(logger::info);


        return filtered;
    }
}
