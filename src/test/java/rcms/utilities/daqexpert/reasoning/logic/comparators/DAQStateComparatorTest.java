package rcms.utilities.daqexpert.reasoning.logic.comparators;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

public class DAQStateComparatorTest {

	@Test
	public void test() {
		DAQStateComparator comparator = new DAQStateComparator();
		Assert.assertTrue(comparator.compare(generate("s1")));
		Assert.assertFalse(comparator.compare(generate("s1")));
		Assert.assertTrue(comparator.compare(generate("s2")));
		Assert.assertFalse(comparator.compare(generate("s2")));
		Assert.assertTrue(comparator.compare(null));
		Assert.assertFalse(comparator.compare(null));
		Assert.assertFalse(comparator.compare(null));
		Assert.assertTrue(comparator.compare(generate("s2")));
		Assert.assertFalse(comparator.compare(generate("s2")));
		Assert.assertTrue(comparator.compare(null));
		Assert.assertFalse(comparator.compare(null));
		Assert.assertTrue(comparator.compare(generate("s3")));
	}

	private DAQ generate(String state) {

		DAQ daq = new DAQ();
		daq.setDaqState(state);

		return daq;
	}

}
