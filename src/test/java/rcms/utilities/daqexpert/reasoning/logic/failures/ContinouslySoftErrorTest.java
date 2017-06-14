package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.Setting;

public class ContinouslySoftErrorTest {

	private static final String FIX = "FixingSoftError";
	private static final String ELSE = "Running";

	ContinouslySoftError lm;

	@Before
	public void before() {
		lm = new ContinouslySoftError();
		Properties p = new Properties();
		p.setProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_COUNT.getKey(), Integer.toString(3));
		p.setProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_PERIOD.getKey(), Integer.toString(100));
		p.setProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_KEEP.getKey(), Integer.toString(5));
		lm.parametrize(p);
	}

	@Test
	public void stuckDoesNotFireTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 1), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 3), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 5), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 7), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 8), null));

		// TODO: check list of previous occ
	}

	@Test
	public void forthOccurrenceSatisfiesTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8), null));
	}

	@Test
	public void connectOccurrencesWithShortBreakTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14), null));
	}

	@Test
	public void toggleOffAfterTimeThresholdTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 15), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 16), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 17), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 18), null));
		// last timestamp of FIX is 14, with threshold 5, 19 should not pass
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 19), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 20), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 21), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 22), null));
	}

	@Test
	public void doNotConnectOccurrencesWithLongerBreakTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 15), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 16), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 17), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 18), null));
		// last timestamp of FIX is 14, with threshold 5, 19 should not pass
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 19), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 20), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 21), null));
		// last FIX timestamps in last 100 are: 14, 12, 10... - it's more than 3
		// - any FIX before 114 will satisfy
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 22), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 23), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 24), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 25), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 26), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 27), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 28), null));
	}

	@Test
	public void resetAfterPeriodTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 15), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 16), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 17), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 18), null));
		// last timestamp of FIX is 14, with threshold 5, 19 should not pass
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 19), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 20), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 21), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 22), null));
		// the last FIX was 14
		// period of 100 passed, next FIX should not satisfy, at least 3 will
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 114), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 115), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 116), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 117), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 118), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 119), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 120), null));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 121), null));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 122), null));
	}

	private DAQ generateSnapshot(String levelZeroState, long lastUpdate) {
		DAQ daq = new DAQ();
		daq.setLevelZeroState(levelZeroState);
		daq.setLastUpdate(lastUpdate);
		return daq;
	}

}
