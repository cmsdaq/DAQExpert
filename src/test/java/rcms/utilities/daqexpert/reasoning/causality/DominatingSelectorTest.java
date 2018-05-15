package rcms.utilities.daqexpert.reasoning.causality;

import org.junit.Test;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class DominatingSelectorTest {

    /**
     * In this case 2 LM do not have 'requirement' relationship. 'Causality' relationship is used.
     * @throws Exception
     */
    @Test
    public void getLeafsFromCausality() throws Exception {
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.VeryHighTcdsInputRate);
        Condition c2 = generateCondition(LogicModuleRegistry.RateTooHigh);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<Condition> result = ds.getLeafsFromCausality(conditionList);
        assertEquals(1, result.size());
        assertThat(result, hasItem(c1));
        assertThat(result, not(hasItem(c2)));
    }

    /**
     *
     * Requirement graph: c1 -> c3, c2 independent
     * Causality graph: c1 -> c2 <- c3
     *
     * Choosing process:
     * - from requirement graph c3 or c2 will be selected
     * - from causality c1 or c3 will be selected
     * - combination of both will yield c3
     *
     *
     * @throws Exception
     */
    @Test
    public void selectDominating() throws Exception {
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.TTSDeadtime);
        Condition c3 = generateCondition(LogicModuleRegistry.FlowchartCase5);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.add(c3);

        CausalityManager cm = new CausalityManager();

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());

        cm.transformToCanonical(a);



        Set<Condition> subResult1 = ds.getLeafsFromUsageGraph(conditionList);
        Set<Condition> subResult2 = ds.getLeafsFromCausality(conditionList);

        assertEquals(2, subResult1.size());
        assertEquals(2, subResult2.size());

        Condition result = ds.selectDominating(conditionList);

        assertEquals(c3, result);


    }

    /**
     * In this case 2 LM do not have 'causality' relation. 'Required' relation is used
     * @throws Exception
     */
    @Test
    public void getLeafsFromUsageGraph() throws Exception {

        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.RuFailed);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<Condition> result = ds.getLeafsFromUsageGraph(conditionList);
        assertEquals(1, result.size());
        assertThat(result, hasItem(c2));
        assertThat(result, not(hasItem(c1)));

    }

    private Condition generateCondition(LogicModuleRegistry logicModuleRegistry) {
        Condition c = new Condition();

        c.setLogicModule(logicModuleRegistry);

        return c;
    }

}