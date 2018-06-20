package rcms.utilities.daqexpert.reasoning.causality;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.collectors.EventRegister;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.processing.ConditionProducer;
import rcms.utilities.daqexpert.reasoning.processing.LogicModuleManager;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Date;

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
        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.VeryHighTcdsInputRate);
        Condition c2 = generateCondition(LogicModuleRegistry.RateTooHigh);
        conditionList.add(c1);
        conditionList.add(c2);

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());
        cm.transformToCanonical(a);

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
        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
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
        assertEquals(2, subResult1.size());

        Set<Condition> subResult2 = ds.getLeafsFromCausality(conditionList);
        assertEquals(1, subResult2.size());

        Condition result = ds.selectDominating(conditionList);

        assertEquals(c3, result);


    }

    @Test
    public void dontConciderConditionsThatEnded() throws Exception {
        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
        DominatingSelector ds = new DominatingSelector();
        Set<Condition> conditionList = new HashSet<>();
        Condition c1 = generateCondition(LogicModuleRegistry.NoRateWhenExpected);
        Condition c2 = generateCondition(LogicModuleRegistry.TTSDeadtime);
        Condition c3 = generateCondition(LogicModuleRegistry.FlowchartCase5);
        conditionList.add(c1);
        conditionList.add(c2);
        conditionList.add(c3);

        c3.setEnd(new Date());

        CausalityManager cm = new CausalityManager();

        conditionList.stream().forEach(c -> c.getLogicModule().getLogicModule().declareRelations());
        Set<CausalityNode> a = conditionList.stream().map(c->c.getLogicModule().getLogicModule()).collect(Collectors.toSet());

        cm.transformToCanonical(a);

        Condition result = ds.selectDominating(conditionList);
        assertEquals(c1, result);

    }

    /**
     * In this case 2 LM do not have 'causality' relation. 'Required' relation is used
     * @throws Exception
     */
    @Test
    public void getLeafsFromUsageGraph() throws Exception {

        Logger.getLogger(DominatingSelector.class).setLevel(Level.DEBUG);
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
        c.setProblematic(true);
        c.setTitle(logicModuleRegistry.name());

        c.setLogicModule(logicModuleRegistry);

        return c;
    }
    /**
     * This test checks that LM can both declare requirement and affected relationship as long as:
     * - if LM A requires B
     * - LM A can affect B
     * - LM A cannot be caused by B - technically can but it will not be taken into account by dominating selection mechanism
     */
    @Test
    public void bothRequirementAndCausalityRelation(){

        LogicModule lm1 = new LogicModuleMock("HLT CPU load mock");
        LogicModule lm2 = new LogicModuleMock("BP from HLT");

        /* This means the LM1 will be used */
        lm1.getAffected().add(lm2);


        Condition c1 = generateCondition(lm1);
        Condition c2 = generateCondition(lm2);

        Set<Condition> conditions = new HashSet<>();
        conditions.add(c1);
        conditions.add(c2);

        DominatingSelector ds = new DominatingSelector();
        Condition dominating = ds.selectDominating(conditions);

        Assert.assertNotNull(dominating);
        Assert.assertEquals(c1, dominating);

        /* This means the LM1 will be used */
        /* The situation should not change after updating required relation*/
        lm1.getRequired().add(lm2);

        CausalityManager cm = new CausalityManager();
        Set<CausalityNode> set = new HashSet<>();
        set.add(lm1);
        set.add(lm2);
        cm.transformToCanonical(set);

        dominating = ds.selectDominating(conditions);

        Assert.assertNotNull(dominating);
        Assert.assertEquals(c1, dominating);

    }


    @Test
    public void transitiveCausalityTest(){

        LogicModule lm1 = new LogicModuleMock("TTS Deadtime");
        LogicModule lm2 = new LogicModuleMock("Partition problem");
        LogicModule lm3 = new LogicModuleMock("Downtime");
        LogicModule lm4 = new LogicModuleMock("Dataflow stuck");

        lm2.getAffected().add(lm4);
        lm4.getAffected().add(lm1);
        lm2.getRequired().add(lm4);


        Condition c1 = generateCondition(lm1);
        Condition c2 = generateCondition(lm2);
        Condition c3 = generateCondition(lm3);
        Condition c4 = generateCondition(lm4);

        Set<Condition> conditions = new HashSet<>();
        conditions.add(c1);
        conditions.add(c2);
        conditions.add(c3);
        conditions.add(c4);

        DominatingSelector ds = new DominatingSelector();

        System.out.println("Usage:");
        Set<Condition> leafsFromUSage = ds.getLeafsFromUsageGraph(conditions);
        leafsFromUSage.stream().forEach(System.out::println);
        Assert.assertEquals(3, leafsFromUSage.size());

        System.out.println("Causality (from all):");
        Set<Condition> leafsFromCausality = ds.getLeafsFromCausality(conditions);
        leafsFromCausality.stream().forEach(System.out::println);
        Assert.assertEquals(2, leafsFromCausality.size());


        // from subset you will get more unresolved nodes (3 instead of 2) - as graph is not completed
        System.out.println("Causality (from subset):");
        Set<Condition> leafsFromCausalitySubset = ds.getLeafsFromCausality(leafsFromUSage);
        leafsFromCausalitySubset.stream().forEach(System.out::println);
        Assert.assertEquals(3, leafsFromCausalitySubset.size());


        Set<Condition> intersection = new HashSet<>(leafsFromUSage); // use the copy constructor
        intersection.retainAll(leafsFromCausality);

        System.out.println("Intersection:");
        intersection.stream().forEach(System.out::println);
        Assert.assertEquals(2, intersection.size());


    }


    @Test
    public void multipleCandidatesTest() throws URISyntaxException {
        DAQ daq = FlowchartCaseTestBase.getSnapshot("1527847792106.json.gz");

        DominatingSelector ds = new DominatingSelector();

        ConditionProducer cp = new ConditionProducer();
        cp.setEventRegister(new EventRegisterMock());

        Application.initialize("src/test/resources/integration.properties");

        LogicModuleManager logicModuleManager = new LogicModuleManager(cp);

        Long originalTimestamp = daq.getLastUpdate();
        daq.setLastUpdate(originalTimestamp - 10000000);
        List<Condition> result = logicModuleManager.runLogicModules(daq, false);


        daq.setLastUpdate(originalTimestamp);
        result.addAll(logicModuleManager.runLogicModules(daq, false));


        Condition dominating = ds.selectDominating(result.stream().filter(c->c.isShow() && c.isProblematic() && !c.isHoldNotifications()).collect(Collectors.toSet()));

        Assert.assertEquals("Partition problem", dominating.getTitle());

    }


    @Test
    public void multipleCandidatesTest2() throws URISyntaxException {
        DAQ daq = FlowchartCaseTestBase.getSnapshot("1528225251698.json.gz");

        DominatingSelector ds = new DominatingSelector();

        ConditionProducer cp = new ConditionProducer();
        cp.setEventRegister(new EventRegisterMock());

        Application.initialize("src/test/resources/integration.properties");

        LogicModuleManager logicModuleManager = new LogicModuleManager(cp);

        Long originalTimestamp = daq.getLastUpdate();
        daq.setLastUpdate(originalTimestamp - 10000000);
        List<Condition> result = logicModuleManager.runLogicModules(daq, false);


        daq.setLastUpdate(originalTimestamp);
        result.addAll(logicModuleManager.runLogicModules(daq, false));


        Condition dominating = ds.selectDominating(result.stream().filter(c->c.isShow() && c.isProblematic() && !c.isHoldNotifications()).collect(Collectors.toSet()));

        Assert.assertEquals("Extreme HLT output bandwidth", dominating.getTitle());

    }
    private Condition generateCondition(LogicModule producer){

        Condition c = new ConditionMock(producer);
        c.setTitle(producer.getName());
        return c;
    }

    class LogicModuleMock extends LogicModule{

        public LogicModuleMock(String name) {
            this.name = name;
        }

    }

    class ConditionMock extends Condition{

        LogicModule producer;

        public ConditionMock(LogicModule producer) {
            this.producer = producer;
            this.setProblematic(true);
        }

        @Override
        public LogicModule getProducer() {
            return producer;
        }
    }

    class EventRegisterMock implements EventRegister {

        @Override
        public void registerBegin(Condition condition) {

        }

        @Override
        public void registerEnd(Condition condition) {

        }

        @Override
        public void registerUpdate(Condition condition) {

        }

        @Override
        public List<ConditionEvent> getEvents() {
            return null;
        }
    }

}