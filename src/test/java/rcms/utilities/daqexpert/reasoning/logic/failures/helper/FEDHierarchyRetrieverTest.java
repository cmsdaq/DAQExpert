package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase1;

public class FEDHierarchyRetrieverTest {

	private final static Logger logger = Logger.getLogger(FEDHierarchyRetriever.class);

	private FED f1;
	private FED f2;
	private FED f3;
	private FED f4;
	private FED f5;
	private TTCPartition p;

	@Before
	public void prepare() {
		f1 = new FED();
		f2 = new FED();
		f3 = new FED();
		f4 = new FED();
		f5 = new FED();

		f1.setFmmMasked(false);
		f2.setFmmMasked(false);
		f3.setFmmMasked(false);
		f4.setFmmMasked(false);
		f5.setFmmMasked(false);

		f1.setFrlMasked(false);
		f2.setFrlMasked(false);
		f3.setFrlMasked(false);
		f4.setFrlMasked(false);
		f5.setFrlMasked(false);

		f1.setSrcIdExpected(1);
		f2.setSrcIdExpected(2);
		f3.setSrcIdExpected(3);
		f4.setSrcIdExpected(4);
		f5.setSrcIdExpected(5);
		List<FED> feds = new ArrayList<FED>();
		feds.add(f1);
		feds.add(f2);
		feds.add(f3);
		feds.add(f4);
		feds.add(f5);

		p = new TTCPartition();
		p.setFeds(feds);
	}

	/* All feds independent [[1:],[2:],[3:],[4:],[5:]] */
	@Test
	public void flatHierarchyTest() {

		Map<FED, Set<FED>> r1 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(5, r1.size());
		assertEquals(0, r1.get(f1).size());
		assertEquals(0, r1.get(f2).size());
		assertEquals(0, r1.get(f3).size());
		assertEquals(0, r1.get(f4).size());
		assertEquals(0, r1.get(f5).size());

	}

	/* put FED2 behind FED1 [[1:2],[3:],[4:],[5:]] */
	@Test
	public void singleFEDBehindOneFEDHierarchyTest() {
		f2.setDependentFeds(Arrays.asList(f1));
		Map<FED, Set<FED>> r2 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(4, r2.size());
		assertEquals(1, r2.get(f1).size());
		assertEquals("FED2 is not a root of hierarchy", false, r2.containsKey(f2));
		assertEquals(0, r2.get(f3).size());
		assertEquals(0, r2.get(f4).size());
		assertEquals(0, r2.get(f5).size());

	}

	/* put FED3 behind FED1 [[1: 3,2],[4:],[5:]] */
	@Test
	public void multipleFEDsBehindOneFEDHierarchyTest() {

		f2.setDependentFeds(Arrays.asList(f1));
		f3.getDependentFeds().add(f1);
		Map<FED, Set<FED>> r3 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(3, r3.size());
		assertEquals(2, r3.get(f1).size());
		assertEquals("FED2 is not a root of hierarchy", false, r3.containsKey(f2));
		assertEquals("FED3 is not a root of hierarchy", false, r3.containsKey(f3));
		assertEquals(0, r3.get(f4).size());
		assertEquals(0, r3.get(f5).size());

	}

	/*
	 * put FED4 behind FED5 [[1: 3, 2],[5: 4]]
	 */
	@Test
	public void multipleGroupsHierarchyTest() {
		f2.setDependentFeds(Arrays.asList(f1));
		f3.getDependentFeds().add(f1);
		f4.getDependentFeds().add(f5);
		Map<FED, Set<FED>> r4 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(2, r4.size());
		assertEquals(2, r4.get(f1).size());
		assertEquals("FED2 is not a root of hierarchy", false, r4.containsKey(f2));
		assertEquals("FED3 is not a root of hierarchy", false, r4.containsKey(f3));
		assertEquals("FED4 is not a root of hierarchy", false, r4.containsKey(f4));
		assertEquals(1, r4.get(f5).size());
	}

	/*
	 * All feds independent one FED only FRL masked [[1:],[M2:],[3:],[4:],[5:]]
	 */
	@Test
	public void oneFedFrlMaskedShouldGetToHierarchyTest() {

		f2.setFrlMasked(true);
		Map<FED, Set<FED>> r1 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(5, r1.size());
		assertEquals(0, r1.get(f1).size());
		assertEquals(0, r1.get(f2).size());
		assertEquals(0, r1.get(f3).size());
		assertEquals(0, r1.get(f4).size());
		assertEquals(0, r1.get(f5).size());

	}

	/*
	 * All feds independent one FED FRL and FMM masked
	 * [[1:],[M2:],[3:],[4:],[5:]]
	 */
	@Test
	public void oneFEDMaskedShouldNotGetToHierarchyTest() {

		f2.setFrlMasked(true);
		f2.setFmmMasked(true);
		Map<FED, Set<FED>> r1 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(4, r1.size());
		assertEquals(0, r1.get(f1).size());
		assertEquals("FED2 should not get to hierarchy", false, r1.containsKey(f2));
		assertEquals(0, r1.get(f3).size());
		assertEquals(0, r1.get(f4).size());
		assertEquals(0, r1.get(f5).size());

	}

	/* hierarchy with one child masked [[1: 3,M2],[4:],[5:]] */
	@Test
	public void multipleFEDsBehindOneFEDAndChildMaskedShouldNotGetToHierarchyTest() {

		f2.setFrlMasked(true);
		f2.setFmmMasked(true);
		f2.setDependentFeds(Arrays.asList(f1));
		f3.getDependentFeds().add(f1);
		Map<FED, Set<FED>> r3 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(3, r3.size());
		assertEquals("Reduced to 1 as FED2 should not get here", 1, r3.get(f1).size());
		assertEquals("FED2 is not a root of hierarchy", false, r3.containsKey(f2));
		assertEquals("FED3 is not a root of hierarchy", false, r3.containsKey(f3));
		assertEquals(0, r3.get(f4).size());
		assertEquals(0, r3.get(f5).size());

	}

	/* hierarchy with one child masked [[M1: 3,2],[4:],[5:]] */
	@Test
	public void rootOfHierarchyMasked() {

		f1.setFrlMasked(true);
		f1.setFmmMasked(true);
		f2.setDependentFeds(Arrays.asList(f1));
		f3.getDependentFeds().add(f1);
		Map<FED, Set<FED>> r3 = FEDHierarchyRetriever.getFEDHierarchy(p);
		assertEquals(4, r3.size());
		assertEquals("FED1 is masked out", false, r3.containsKey(f1));
		assertEquals(0, r3.get(f2).size());
		assertEquals(0, r3.get(f3).size());
		assertEquals(0, r3.get(f4).size());
		assertEquals(0, r3.get(f5).size());

	}

	@Test
	public void realHierarchyTest() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1498190405943.smile");

		Map<String, Map<String, Map<FED, Set<FED>>>> subsystemsResult = new HashMap<>();

		int totalGroups = 0;
		for (SubSystem s : snapshot.getSubSystems()) {

			Map<String, Map<FED, Set<FED>>> partitionsResult = new HashMap<>();
			subsystemsResult.put(s.getName(), partitionsResult);
			int subsystemGroups = 0;
			System.out.println("SUBSYSTEM " + s.getName());

			for (TTCPartition p : s.getTtcPartitions()) {

				if (p.getName().equals("HBHEA")) {
					// Logger.getLogger(FEDHierarchyRetriever.class).setLevel(Level.TRACE);
				} else {
					// Logger.getLogger(FEDHierarchyRetriever.class).setLevel(Level.INFO);
				}
				Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(p);
				partitionsResult.put(p.getName(), h);

				int partitionGroups = 0;
				System.out.println("  PARTITION " + p.getName());

				for (Map.Entry<FED, Set<FED>> e : h.entrySet()) {
					String deps = "";
					boolean notFirst = false;
					for (FED dep : e.getValue()) {
						if (notFirst) {
							deps += ", ";
						}
						notFirst = true;
						deps += dep.getSrcIdExpected();
					}
					totalGroups++;
					subsystemGroups++;
					partitionGroups++;
					System.out.println(
							"    [" + e.getKey().getSrcIdExpected() + "]" + (deps.equals("") ? "" : ": " + deps));
				}

				System.out.println("    #" + partitionGroups + " groups in partition " + p.getName());
			}
			System.out.println("  #" + subsystemGroups + " groups in subsystem " + s.getName());
		}
		System.out.println("#" + totalGroups + " groups in DAQ");

		assertEquals("Number of subsystems in the result", 14, subsystemsResult.size());

		/*
		 * TRACKER has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 4, subsystemsResult.get("TRACKER").size());
		assertEquals("FED groups in partition", 132, subsystemsResult.get("TRACKER").get("TOB").size());
		assertEquals("FED groups in partition", 96, subsystemsResult.get("TRACKER").get("TEC-").size());
		assertEquals("FED groups in partition", 112, subsystemsResult.get("TRACKER").get("TIBTID").size());
		assertEquals("FED groups in partition", 96, subsystemsResult.get("TRACKER").get("TEC+").size());

		/*
		 * CTPPS_TOT has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 1, subsystemsResult.get("CTPPS_TOT").size());
		assertEquals("FED groups in partition", 4, subsystemsResult.get("CTPPS_TOT").get("TOTDET").size());

		/*
		 * ES has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 2, subsystemsResult.get("ES").size());
		assertEquals("FED groups in partition", 20, subsystemsResult.get("ES").get("ES-").size());
		assertEquals("FED groups in partition", 20, subsystemsResult.get("ES").get("ES+").size());

		/*
		 * ECAL has flat hierarchy (exception - there is oner root pseudoFED but
		 * each FED has both TTS and SLINK
		 */
		assertEquals("Partitions in the subsystem", 4, subsystemsResult.get("ECAL").size());
		assertEquals("FED groups in partition", 10, subsystemsResult.get("ECAL").get("EE-").size());
		assertEquals("FED groups in partition", 19, subsystemsResult.get("ECAL").get("EB-").size());
		assertEquals("FED groups in partition", 19, subsystemsResult.get("ECAL").get("EB+").size());
		assertEquals("FED groups in partition", 10, subsystemsResult.get("ECAL").get("EE+").size());

		/*
		 * RPC has tree hierarchy
		 */
		assertEquals("Partitions in the subsystem", 1, subsystemsResult.get("RPC").size());

		/* hierarchy of RPC:RPC */
		assertEquals("FED groups in partition", 1, subsystemsResult.get("RPC").get("RPC").size());
		assertEquals("Root pseudoFED id", 793,
				subsystemsResult.get("RPC").get("RPC").keySet().iterator().next().getSrcIdExpected());
		assertEquals("Dep feds behind pseudo FED", 3,
				subsystemsResult.get("RPC").get("RPC").values().iterator().next().size());

		/*
		 * TCDS has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 1, subsystemsResult.get("TCDS").size());
		assertEquals("FED groups in partition", 1, subsystemsResult.get("TCDS").get("CPM-PRI").size());

		/*
		 * DT has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 4, subsystemsResult.get("DT").size());
		assertEquals("FED groups in partition", 2, subsystemsResult.get("DT").get("DT-").size());
		assertEquals("FED groups in partition", 5, subsystemsResult.get("DT").get("TWINMUX").size());
		assertEquals("FED groups in partition", 2, subsystemsResult.get("DT").get("DT+").size());
		assertEquals("FED groups in partition", 1, subsystemsResult.get("DT").get("DT0").size());

		/*
		 * CTPPS has tree hierarchy
		 */
		assertEquals("Partitions in the subsystem", 1, subsystemsResult.get("CTPPS").size());

		/* hierarchy of CTPPS:CTPPS */
		assertEquals("FED groups in partition", 1, subsystemsResult.get("CTPPS").get("CTPPS").size());
		assertEquals("Root pseudoFED id", 11462,
				subsystemsResult.get("CTPPS").get("CTPPS").keySet().iterator().next().getSrcIdExpected());
		assertEquals("Dep feds behind pseudo FED", 2,
				subsystemsResult.get("CTPPS").get("CTPPS").values().iterator().next().size());

		/*
		 * TRG has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 5, subsystemsResult.get("TRG").size());
		assertEquals("FED groups in partition", 1, subsystemsResult.get("TRG").get("CALTRIGUP").size());
		assertEquals("FED groups in partition", 3, subsystemsResult.get("TRG").get("RCT").size());
		assertEquals("FED groups in partition", 0, subsystemsResult.get("TRG").get("GTUPSPARE").size());
		assertEquals("FED groups in partition", 1, subsystemsResult.get("TRG").get("GTUP").size());
		assertEquals("FED groups in partition", 8, subsystemsResult.get("TRG").get("MUTFUP").size());

		/*
		 * CSC has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 2, subsystemsResult.get("CSC").size());
		assertEquals("FED groups in partition", 18, subsystemsResult.get("CSC").get("CSC-").size());
		assertEquals("FED groups in partition", 18, subsystemsResult.get("CSC").get("CSC+").size());

		/*
		 * SCAL has flat hierarchy
		 */
		assertEquals("Partitions in the subsystem", 1, subsystemsResult.get("SCAL").size());
		assertEquals("FED groups in partition", 1, subsystemsResult.get("SCAL").get("SCAL").size());

		/*
		 * HCAL has tree hierarchy
		 */
		assertEquals("Partitions in the subsystem", 5, subsystemsResult.get("HCAL").size());

		/* hierarchy of HCAL:HBHEC */
		assertEquals("FED groups in partition", 3, subsystemsResult.get("HCAL").get("HBHEC").size());

		assertThat(subsystemsResult.get("HCAL").get("HBHEC").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11112))));
		assertThat(subsystemsResult.get("HCAL").get("HBHEC").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11114))));
		assertThat(subsystemsResult.get("HCAL").get("HBHEC").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11116))));

		// every HBHEC group has 2 elements
		Iterator<Set<FED>> i = subsystemsResult.get("HCAL").get("HBHEC").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 2, i.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i.next().size());

		/* hierarchy of HCAL:HO */
		assertEquals("FED groups in partition", 8, subsystemsResult.get("HCAL").get("HO").size());

		/* hierarchy of HCAL:HBHEA */
		assertEquals("FED groups in partition", 3, subsystemsResult.get("HCAL").get("HBHEA").size());

		assertThat(subsystemsResult.get("HCAL").get("HBHEA").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11100))));
		assertThat(subsystemsResult.get("HCAL").get("HBHEA").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11102))));
		assertThat(subsystemsResult.get("HCAL").get("HBHEA").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11104))));

		// every HBHEA group has 2 elements
		Iterator<Set<FED>> i2 = subsystemsResult.get("HCAL").get("HBHEA").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 2, i2.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i2.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i2.next().size());

		/* hierarchy of HCAL:HBHEB */
		assertEquals("FED groups in partition", 3, subsystemsResult.get("HCAL").get("HBHEB").size());

		assertThat(subsystemsResult.get("HCAL").get("HBHEB").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11110))));
		assertThat(subsystemsResult.get("HCAL").get("HBHEB").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11106))));
		assertThat(subsystemsResult.get("HCAL").get("HBHEB").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11108))));

		// every HBHEB group has 2 elements
		Iterator<Set<FED>> i3 = subsystemsResult.get("HCAL").get("HBHEB").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 2, i3.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i3.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i3.next().size());

		/* hierarchy of HCAL:HCALLASER */
		assertEquals("FED groups in partition", 0, subsystemsResult.get("HCAL").get("HCALLASER").size());

		/*
		 * HF has tree hierarchy
		 */
		assertEquals("Partitions in the subsystem", 1, subsystemsResult.get("HF").size());

		/* hierarchy of HF:HF */
		assertEquals("FED groups in partition", 3, subsystemsResult.get("HF").get("HF").size());

		assertThat(subsystemsResult.get("HF").get("HF").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11118))));
		assertThat(subsystemsResult.get("HF").get("HF").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11120))));
		assertThat(subsystemsResult.get("HF").get("HF").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11122))));

		// every HF group has 2 elements
		Iterator<Set<FED>> i4 = subsystemsResult.get("HF").get("HF").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 2, i4.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i4.next().size());
		assertEquals("Dep feds behind pseudo FED", 2, i4.next().size());

		/*
		 * PIXEL has tree hierarchy
		 */
		assertEquals("Partitions in the subsystem", 4, subsystemsResult.get("PIXEL").size());

		/* hierarchy of PIXEL:BPIXP */
		assertEquals("FED groups in partition", 4, subsystemsResult.get("PIXEL").get("BPIXP").size());

		assertThat(subsystemsResult.get("PIXEL").get("BPIXP").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11236))));
		assertThat(subsystemsResult.get("PIXEL").get("BPIXP").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11224))));
		assertThat(subsystemsResult.get("PIXEL").get("BPIXP").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11212))));
		assertThat(subsystemsResult.get("PIXEL").get("BPIXP").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11200))));

		// every BPIXP group has 10 elements
		Iterator<Set<FED>> i5 = subsystemsResult.get("PIXEL").get("BPIXP").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 10, i5.next().size());
		assertEquals("Dep feds behind pseudo FED", 10, i5.next().size());
		assertEquals("Dep feds behind pseudo FED", 10, i5.next().size());
		assertEquals("Dep feds behind pseudo FED", 10, i5.next().size());

		/* hierarchy of PIXEL:FPIXP */
		assertEquals("FED groups in partition", 2, subsystemsResult.get("PIXEL").get("FPIXP").size());

		assertThat(subsystemsResult.get("PIXEL").get("FPIXP").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11296))));
		assertThat(subsystemsResult.get("PIXEL").get("FPIXP").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11308))));

		// every FPIXP group has 7 elements
		Iterator<Set<FED>> i6 = subsystemsResult.get("PIXEL").get("FPIXP").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 7, i6.next().size());
		assertEquals("Dep feds behind pseudo FED", 7, i6.next().size());

		/* hierarchy of PIXEL:FPIXM */
		assertEquals("FED groups in partition", 2, subsystemsResult.get("PIXEL").get("FPIXM").size());

		assertThat(subsystemsResult.get("PIXEL").get("FPIXM").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11320))));
		assertThat(subsystemsResult.get("PIXEL").get("FPIXM").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11332))));

		// every FPIXM group has 7 elements
		Iterator<Set<FED>> i7 = subsystemsResult.get("PIXEL").get("FPIXM").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 7, i7.next().size());
		assertEquals("Dep feds behind pseudo FED", 7, i7.next().size());

		/* hierarchy of PIXEL:BPIXM */
		assertEquals("FED groups in partition", 4, subsystemsResult.get("PIXEL").get("BPIXM").size());

		assertThat(subsystemsResult.get("PIXEL").get("BPIXM").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11272))));
		assertThat(subsystemsResult.get("PIXEL").get("BPIXM").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11248))));
		assertThat(subsystemsResult.get("PIXEL").get("BPIXM").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11260))));
		assertThat(subsystemsResult.get("PIXEL").get("BPIXM").keySet(),
				hasItem(Matchers.<FED> hasProperty("srcIdExpected", is(11284))));

		// every BPIXM group has 10 elements
		Iterator<Set<FED>> i8 = subsystemsResult.get("PIXEL").get("BPIXM").values().iterator();
		assertEquals("Dep feds behind pseudo FED", 10, i8.next().size());
		assertEquals("Dep feds behind pseudo FED", 10, i8.next().size());
		assertEquals("Dep feds behind pseudo FED", 10, i8.next().size());
		assertEquals("Dep feds behind pseudo FED", 10, i8.next().size());

	}

	/**
	 * Copied from FlowchartCaseTestBase - refactor
	 */
	public DAQ getSnapshot(String fname) throws URISyntaxException {

		StructureSerializer serializer = new StructureSerializer();

		URL url = FlowchartCase1.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath(), PersistenceFormat.SMILE);
	}
}
