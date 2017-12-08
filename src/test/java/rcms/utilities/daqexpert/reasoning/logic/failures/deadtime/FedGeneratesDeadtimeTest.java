package rcms.utilities.daqexpert.reasoning.logic.failures.deadtime;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.processing.context.ReusableContextEntry;
import rcms.utilities.daqexpert.processing.context.functions.FedPrinter;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

        /** Note that DEADTIME context value is not filled in this test as it comes from other LM. It's not the scope of this test. This is covered in integration tests */
        Assert.assertEquals("FED <strong>2</strong> generates deadtime {{DEADTIME}}, the threshold is 2.0%. There is no backpressure from DAQ on this FED.", module.getDescriptionWithContext());

    }

    private Map<String, Output> getOutputFromRequiredLm(Properties p, DAQ daq, Predicate<FED> predicate) {
        Map<String, Output> r = new HashMap<>();

        r.put(ExpectedRate.class.getSimpleName(), new Output(true));

        FEDDeadtime fd = new FEDDeadtime();
        fd.parametrize(p);

        ContextHandler ch = new ContextHandler();
        daq.getFeds().stream().filter(predicate).forEach(f -> ch.registerObject("FED", f, new FedPrinter()));
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


        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(), "2");
        p.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(), "2");
        module.parametrize(p);


        DAQ snapshot = new DAQ();
        TTCPartition partition = new TTCPartition();
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
        ch.registerObject("FED", pseudoFed, new FedPrinter());
        fedDeadtimeOutput.setContext(ch.getContext());
        r.put(FEDDeadtime.class.getSimpleName(), fedDeadtimeOutput);

        snapshot.setTtcPartitions(Arrays.asList(partition));
        Assert.assertTrue(module.satisfied(snapshot, r));

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