package rcms.utilities.daqexpert.processing;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqexpert.servlets.DummyDAQ;

public class SortedArrayListTest {

	@Test
	public void simpleTest() {
		SortedArrayList<String> test = new SortedArrayList<String>();

		test.add("d");
		Assert.assertEquals("d", test.get(0));
		test.add("a");
		Assert.assertEquals("a", test.get(0));
		Assert.assertEquals("d", test.get(1));
		test.add("c");
		Assert.assertEquals("a", test.get(0));
		Assert.assertEquals("c", test.get(1));
		Assert.assertEquals("d", test.get(2));
		test.add("b");
		Assert.assertEquals("a", test.get(0));
		Assert.assertEquals("b", test.get(1));
		Assert.assertEquals("c", test.get(2));
		Assert.assertEquals("d", test.get(3));
		test.add("e");
		Assert.assertEquals("a", test.get(0));
		Assert.assertEquals("b", test.get(1));
		Assert.assertEquals("c", test.get(2));
		Assert.assertEquals("d", test.get(3));
		Assert.assertEquals("e", test.get(4));
	}

	@Test
	public void entryTest() {
		SortedArrayList<DummyDAQ> test = new SortedArrayList<DummyDAQ>();

		DummyDAQ daq1 = new DummyDAQ();
		daq1.setLastUpdate(1);
		DummyDAQ daq2 = new DummyDAQ();
		daq2.setLastUpdate(2);
		DummyDAQ daq3 = new DummyDAQ();
		daq3.setLastUpdate(3);
		DummyDAQ daq4 = new DummyDAQ();
		daq4.setLastUpdate(4);
		DummyDAQ daq5 = new DummyDAQ();
		daq5.setLastUpdate(5);

		test.add(daq2);
		Assert.assertEquals(daq2, test.get(0));

		test.add(daq1);
		Assert.assertEquals(daq1, test.get(0));
		Assert.assertEquals(daq2, test.get(1));

		test.add(daq5);
		Assert.assertEquals(daq1, test.get(0));
		Assert.assertEquals(daq2, test.get(1));
		Assert.assertEquals(daq5, test.get(2));

		test.add(daq4);
		Assert.assertEquals(daq1, test.get(0));
		Assert.assertEquals(daq2, test.get(1));
		Assert.assertEquals(daq4, test.get(2));
		Assert.assertEquals(daq5, test.get(3));

		test.add(daq3);
		Assert.assertEquals(daq1, test.get(0));
		Assert.assertEquals(daq2, test.get(1));
		Assert.assertEquals(daq3, test.get(2));
		Assert.assertEquals(daq4, test.get(3));
		Assert.assertEquals(daq5, test.get(4));
	}

	@Test
	public void multipleEntryTest() {
		SortedArrayList<DummyDAQ> test = new SortedArrayList<>();

		DummyDAQ daq1 = new DummyDAQ();
		daq1.setLastUpdate(1);
		DummyDAQ daq2 = new DummyDAQ();
		daq2.setLastUpdate(2);
		DummyDAQ daq3 = new DummyDAQ();
		daq3.setLastUpdate(3);
		DummyDAQ daq4 = new DummyDAQ();
		daq4.setLastUpdate(4);
		DummyDAQ daq5 = new DummyDAQ();
		daq5.setLastUpdate(5);

		List<DummyDAQ> list1 = new ArrayList<>();

		list1.add(daq2);
		list1.add(daq5);
		list1.add(daq3);

		List<DummyDAQ> list2 = new ArrayList<>();

		list2.add(daq4);
		list2.add(daq1);

		test.addAll(list1);
		Assert.assertEquals(daq2, test.get(0));
		Assert.assertEquals(daq3, test.get(1));
		Assert.assertEquals(daq5, test.get(2));

		test.addAll(list2);
		Assert.assertEquals(daq1, test.get(0));
		Assert.assertEquals(daq2, test.get(1));
		Assert.assertEquals(daq3, test.get(2));
		Assert.assertEquals(daq4, test.get(3));
		Assert.assertEquals(daq5, test.get(4));

	}
}
