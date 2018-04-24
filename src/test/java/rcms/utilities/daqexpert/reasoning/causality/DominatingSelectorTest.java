package rcms.utilities.daqexpert.reasoning.causality;

import org.junit.Test;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class DominatingSelectorTest {
    @Test
    public void selectDominating() throws Exception {
    }

    @Test
    public void getLeafsFromUsageGraph() throws Exception {

        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.RuFailed);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRequired());
        Set<Condition> result = ds.getLeafsFromUsageGraph(conditionList);
        assertThat(result, hasItem(c2));
        assertThat(result, not(hasItem(c1)));

    }

    private Condition generateCondition(LogicModuleRegistry logicModuleRegistry) {
        Condition c = new Condition();

        c.setLogicModule(logicModuleRegistry);

        return c;
    }

}