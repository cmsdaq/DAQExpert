package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;

public class FEDHierarchyRetrieverTest {

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
		assertEquals("FED2 is no longer accessible as key", false, r2.containsKey(f2));
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
		assertEquals("FED2 is no longer accessible as key", false, r3.containsKey(f2));
		assertEquals("FED3 is no longer accessible as key", false, r3.containsKey(f3));
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
		assertEquals("FED2 is no longer accessible as key", false, r4.containsKey(f2));
		assertEquals("FED3 is no longer accessible as key", false, r4.containsKey(f3));
		assertEquals("FED4 is no longer accessible as key", false, r4.containsKey(f4));
		assertEquals(1, r4.get(f5).size());
	}
}
