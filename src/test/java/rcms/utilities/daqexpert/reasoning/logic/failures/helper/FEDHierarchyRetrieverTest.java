package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
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

		DAQ snapshot = getSnapshot("1497562174081.smile");

		int totalGroups = 0;
		for (SubSystem s : snapshot.getSubSystems()) {

			int subsystemGroups = 0;
			System.out.println("SUBSYSTEM " + s.getName());

			for (TTCPartition p : s.getTtcPartitions()) {
				Map<FED, Set<FED>> h = FEDHierarchyRetriever.getFEDHierarchy(p);

				int partitionGroups = 0;
				System.out.println("  PARTITION" + p.getName());

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
