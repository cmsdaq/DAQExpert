package rcms.utilities.daqexpert.websocket;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.causality.CausalityManager;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConditionDashboardTest {

    static Date t1 = DatatypeConverter.parseDateTime("2017-03-09T10:00:00Z").getTime();
    static Date t2 = DatatypeConverter.parseDateTime("2017-03-09T10:00:01Z").getTime();

    private static Long id = 0L;

    static Condition c1 = generateCondition("c1", LogicModuleRegistry.VeryHighTcdsInputRate, t1);
    static Condition c2 = generateCondition("c2", LogicModuleRegistry.RateTooHigh, t1);
    static Condition c3 = generateCondition("c3", LogicModuleRegistry.FlowchartCase5, t2);
    static Condition c4 = generateCondition("c4", LogicModuleRegistry.NoRateWhenExpected, t2);
    static Condition c5 = generateCondition("c5", LogicModuleRegistry.NoRateWhenExpected, t2);

    @BeforeClass
    public static void prepare(){

        List<Condition> conditionList = new ArrayList<>();
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.add(c3);
        conditionList.add(c4);

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());
        cm.transformToCanonical(a);
    }


    @Test
    public void communicationHandlerTest(){

        ConditionDashboard conditionDashboard = new ConditionDashboard(5);
        SessionHandlerStub conditionSessionHandler = new SessionHandlerStub(conditionDashboard);
        conditionDashboard.setSessionHander(conditionSessionHandler);

        conditionDashboard.update(get(c1),0L);
        assertThat(conditionSessionHandler.recentConditionSignalsReceived, hasItem(equalTo(Arrays.asList(0L))));
        assertThat(conditionSessionHandler.dominatingSignalsReceived, contains(equalTo(0L)));


        conditionDashboard.update(get(c2),0L);
        assertThat(conditionSessionHandler.recentConditionSignalsReceived, allOf(
                hasItem(equalTo(Arrays.asList(0L))),
                hasItem(equalTo(Arrays.asList(1L)))
        ));
        assertThat(conditionSessionHandler.dominatingSignalsReceived, contains(equalTo(0L)));


        conditionDashboard.update(get(c3),2L);
        assertThat(conditionSessionHandler.recentConditionSignalsReceived, allOf(
                hasItem(equalTo(Arrays.asList(0L))),
                hasItem(equalTo(Arrays.asList(1L))),
                hasItem(equalTo(Arrays.asList(2L)))
        ));
        assertThat(conditionSessionHandler.dominatingSignalsReceived, contains(
                equalTo(0L),
                equalTo(2L)
        ));

        conditionDashboard.update(get(c4, c5),4L);
        assertThat(conditionSessionHandler.recentConditionSignalsReceived, allOf(
                hasItem(equalTo(Arrays.asList(0L))),
                hasItem(equalTo(Arrays.asList(1L))),
                hasItem(equalTo(Arrays.asList(2L))),
                hasItem(equalTo(Arrays.asList(3L, 4L)))
        ));
        assertThat(conditionSessionHandler.dominatingSignalsReceived, contains(
                equalTo(0L),
                equalTo(2L),
                equalTo(4L)
        ));


        conditionDashboard.update(c1, null);

        assertThat(conditionSessionHandler.updatedConditionSignalsReceived, contains(equalTo(0L)));


    }

    private Set<Condition> get(Condition... conditions){
        return Arrays.asList(conditions).stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Test
    public void simpleScenarioTest() {

        ConditionDashboard conditionDashboard = new ConditionDashboard(5);

        Set<Condition> conditions = new LinkedHashSet<>();

		/* 1 Change - new condition(s) start/end */
        conditions.add(c4);
        conditionDashboard.update(conditions,3L);
        conditions.clear();

		/* 1 Result */
        Assert.assertEquals("Conditions same priority and date -> first", c4, conditionDashboard.getCurrentCondition());
        Assert.assertEquals(1, conditionDashboard.getCurrentConditions().size());
        Assert.assertEquals(0, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 2 Change - new condition(s) start/end */
        conditions.add(c3);
        conditionDashboard.update(conditions,2L);
        conditions.clear();

		/* 2 Result */
        Assert.assertEquals( c3, conditionDashboard.getCurrentCondition());
        Assert.assertEquals(2, conditionDashboard.getCurrentConditions().size());
        Assert.assertEquals(1, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 3 Change - new condition(s) start/end */
        conditions.add(c1);
        conditions.add(c2);
        conditionDashboard.update(conditions,2L);
        conditions.clear();

		/* 3 Result */
        Assert.assertEquals( c3, conditionDashboard.getCurrentCondition());
        Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
        Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 4 Change - new condition(s) start/end */
        c3.setEnd(new Date());
        conditions.remove(c3);
        conditionDashboard.update(conditions,0L);
        conditions.clear();

		/* 4 Result */
        Assert.assertEquals( c1, conditionDashboard.getCurrentCondition());
        Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
        Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());


		/* 4 Change - new condition(s) start/end */
        c1.setEnd(new Date());
        c4.setEnd(new Date());
        conditions.remove(c1);
        conditions.remove(c4);
        conditionDashboard.update(conditions,1L);
        conditions.clear();

		/* 5 Result */
        Assert.assertEquals(c2, conditionDashboard.getCurrentCondition());
        Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
        Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

		/* 6 no more conditions */
        conditions.clear();
        conditionDashboard.update(conditions,1L);
        conditions.clear();

		/* 6 Only recent conditions */
        Assert.assertEquals(c2, conditionDashboard.getCurrentCondition());
        Assert.assertEquals(4, conditionDashboard.getCurrentConditions().size());
        Assert.assertEquals(3, conditionDashboard.getConditionsWithoutDominatingCondition().size());

        Assert.assertNotNull(conditionDashboard.toString());

    }

    public static Condition generateCondition(String title, LogicModuleRegistry lm, Date t) {
        Condition condition = new Condition();
        condition.setId(id++);
        condition.setTitle(title);
        condition.setPriority(ConditionPriority.DEFAULTT);
        condition.setLogicModule(lm);
        condition.setShow(true);
        condition.setMature(true);
        condition.setProblematic(true);
        condition.setStart(t);
        return condition;
    }

    class SessionHandlerStub extends ConditionSessionHandler{


        public List<Long> dominatingSignalsReceived;
        public List<Long> updatedConditionSignalsReceived;
        public List<List<Long>> recentConditionSignalsReceived;


        public SessionHandlerStub(ConditionDashboard conditionDashboard) {
            super(conditionDashboard);

            dominatingSignalsReceived = new ArrayList<>();
            updatedConditionSignalsReceived = new ArrayList<>();
            recentConditionSignalsReceived = new ArrayList<>();
        }


        @Override
        public void handleDominatingConditionChange(Condition dominatingCondition) {
            dominatingSignalsReceived.add(dominatingCondition.getId());
        }

        @Override
        public void handleConditionUpdate(Condition updatedCondition) {
            updatedConditionSignalsReceived.add(updatedCondition.getId());
        }

        @Override
        public void handleRecentConditionsChange(Collection<Condition> recentConditions) {
            recentConditionSignalsReceived.add(recentConditions.stream().map(c->c.getId()).sorted().collect(Collectors.toList()));
        }
    }


}
