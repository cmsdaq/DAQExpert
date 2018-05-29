package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;
import rcms.utilities.daqexpert.processing.context.functions.FedPrinter;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.HltOutputBandwidthExtreme;
import rcms.utilities.daqexpert.reasoning.logic.failures.HltOutputBandwidthTooHigh;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;

public class FedGeneratesDeadtimeTest {
    @Test
    public void satisfied() throws Exception {
        Map<String, Output> r;

        FedGeneratesDeadtime module = new FedGeneratesDeadtime();
        Properties p = new Properties();
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");

        module.parametrize(p);


        DAQ d1 = mockTestObject(0, 0);
        DAQ d2 = mockTestObject(2, 2);
        DAQ d3 = mockTestObject(3, 1);
        DAQ d4 = mockTestObject(3, 3);


        r = getOutputFromRequiredLm(p, d1, f -> f.getSrcIdExpected() == 0);
        Assert.assertFalse(module.satisfied(d1, r));

        r = getOutputFromRequiredLm(p, d2, f -> f.getSrcIdExpected() == 0);
        Assert.assertFalse(module.satisfied(d2, r));

        r = getOutputFromRequiredLm(p, d3, f -> f.getSrcIdExpected() == 2);
        Assert.assertTrue(module.satisfied(d3, r));

        r = getOutputFromRequiredLm(p, d4, f -> f.getSrcIdExpected() == 2);
        Assert.assertFalse(module.satisfied(d4, r));


        ContextHandler.highlightMarkup = false;

        /** Note that DEADTIME context value is not filled in this test as it comes from other LM. It's not the scope of this test. This is covered in integration tests */
        Assert.assertEquals("FED 2 generates deadtime 3%, the threshold is 2.0%. There is no backpressure from DAQ on this FED. FED belongs to partition testpartition in subsystem testsubsystem", module.getDescriptionWithContext());

    }

    private Map<String, Output> getOutputFromRequiredLm(Properties p, DAQ daq, Predicate<FED> predicate) {
        Map<String, Output> r = new HashMap<>();

        r.put(StableBeams.class.getSimpleName(), new Output(true));
        r.put(ExpectedRate.class.getSimpleName(), new Output(true));

        FEDDeadtime fd = new FEDDeadtime();
        fd.parametrize(p);

        ContextHandler ch = new ContextHandler();
        daq.getFeds().stream().filter(predicate).forEach(f -> ch.registerObject("PROBLEM-FED", f, new FedPrinter()));
        Output o = new Output(true);
        o.setContext(ch.getContext());

        r.put(FEDDeadtime.class.getSimpleName(), o);

        return r;
    }


    @Test
    public void pseudoFedHierarchyTest() throws Exception {

        FedGeneratesDeadtime module = new FedGeneratesDeadtime();
        Properties p = new Properties();
        Map<String, Output> r = new HashMap<>();

        r.put(StableBeams.class.getSimpleName(), new Output(false));


        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(p);


        DAQ snapshot = new DAQ();
        TTCPartition partition = new TTCPartition();
        partition.setName("P1");
        SubSystem subsystem = new SubSystem();
        subsystem.setName("S1");
        partition.setSubsystem(subsystem);
        Set<FED> feds = new HashSet<>();

        FED pseudoFed = mockTestObject(10000, 10, 0, partition);
        FED fed = mockTestObject(1, 0, 0, partition);
        fed.setDependentFeds(Arrays.asList(pseudoFed));
        feds.add(fed);
        partition.setFeds(new ArrayList<>(feds));

        Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(partition);
        System.out.println(h);

        for (Map.Entry<FED, Set<FED>> e : h.entrySet()) {
            String deps = "";
            boolean notFirst = false;
            for (FED dep : e.getValue()) {
                if (notFirst) {
                    deps += ", ";
                }
                notFirst = true;
                deps += dep.getSrcIdExpected() + "dt: " + dep.getPercentBackpressure();
            }
            System.out.println(
                    "    [" + e.getKey().getSrcIdExpected() + "bp: " + (e.getKey().getPercentBusy() + e.getKey().getPercentWarning()) + "]" + (deps.equals("") ? "" : ": " + deps));
        }

        snapshot.setFeds(feds);
        snapshot.getFeds();


        Output fedDeadtimeOutput = new Output(true);
        ContextHandler ch = new ContextHandler();
        ch.registerObject("PROBLEM-FED", pseudoFed, new FedPrinter());
        fedDeadtimeOutput.setContext(ch.getContext());
        r.put(FEDDeadtime.class.getSimpleName(), fedDeadtimeOutput);

        snapshot.setTtcPartitions(Arrays.asList(partition));
        Assert.assertTrue(module.satisfied(snapshot, r));

        Assert.assertEquals(module.getContextHandler().getContext().getTextRepresentation("PROBLEM-PARTITION"), "P1");
        Assert.assertEquals(module.getContextHandler().getContext().getTextRepresentation("PROBLEM-SUBSYSTEM"), "S1");

    }


    @Test
    public void test() throws URISyntaxException {

        Logger.getLogger(FedGeneratesDeadtime.class).setLevel(Level.INFO);
        Properties properties = new Properties();
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");



        DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1510723561771.json.gz");

        Map<String, Output> results = new HashMap<>();

        Context context = new Context();
        ObjectContextEntry<FED> contextEntry = new ObjectContextEntry<>();
        contextEntry.update(snapshot.getFeds().stream().filter(f->f.getSrcIdExpected() == 106 ).findFirst().get(), "106");
        context.getContextEntryMap().put("PROBLEM-FED", contextEntry);

        results.put(StableBeams.class.getSimpleName(), new Output(true));
        results.put(FEDDeadtime.class.getSimpleName(), new Output(true, context));


        KnownFailure fedGeneratesDeadtime = new FedGeneratesDeadtime();
        ((Parameterizable) fedGeneratesDeadtime).parametrize(properties);


        ContextHandler.highlightMarkup = false;

        Assert.assertTrue(fedGeneratesDeadtime.satisfied(snapshot, results));
        Assert.assertEquals("FED 106 generates deadtime 7.5%, the threshold is 2.0%. There is no backpressure from DAQ on this FED. FED belongs to partition TIBTID in subsystem TRACKER",
                fedGeneratesDeadtime.getDescriptionWithContext());


    }


    private DAQ mockTestObject(float deadtime, float backpressure) {
        DAQ snapshot = new DAQ();

        Set<FED> feds = new HashSet<>();

        SubSystem s = new SubSystem();
        s.setName("testsubsystem");
        TTCPartition p = new TTCPartition();
        p.setName("testpartition");
        p.setSubsystem(s);

        feds.add(mockTestObject(1, 0, 0, p));
        feds.add(mockTestObject(2, deadtime, backpressure, p));
        feds.add(mockTestObject(3, 0, 0, p));
        snapshot.setFeds(feds);


        p.setFeds(new ArrayList<>(feds));
        snapshot.setTtcPartitions(Arrays.asList(p));

        return snapshot;
    }

    private FED mockTestObject(int id, float deadtime, float backpressure, TTCPartition p) {
        FED fed = new FED();
        fed.setSrcIdExpected(id);
        fed.setPercentBackpressure(backpressure);
        fed.setPercentBusy(deadtime);
        fed.setTtcp(p);
        return fed;
    }

}