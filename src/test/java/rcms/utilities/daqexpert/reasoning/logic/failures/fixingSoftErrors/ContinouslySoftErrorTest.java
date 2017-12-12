package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.context.ReusableContextEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

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

		Assert.assertNull(lm.getContextHandler().getContext().get("SUBSYSTEM"));
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

		Set<String> problematicSubsystems = ((ReusableContextEntry<String>)lm.getContextHandler().getContext().getContextEntryMap().get("SUBSYSTEM")).getObjectSet() ;
		Assert.assertNotNull(problematicSubsystems);
		Assert.assertEquals(2, problematicSubsystems.size());
		assertThat(problematicSubsystems, hasItem(Matchers.<String> is("B 4 time(s)")));
		assertThat(problematicSubsystems, hasItem(Matchers.<String> is("C 4 time(s)")));
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
		SubSystem subsystem1 = new SubSystem();
		SubSystem subsystem2 = new SubSystem();
		SubSystem subsystem3 = new SubSystem();
		subsystem1.setName("A");
		subsystem2.setName("B");
		subsystem3.setName("C");

		subsystem1.setStatus("A");
		subsystem2.setStatus("RunningSoftErrorDetected");
		subsystem3.setStatus("FixingSoftError");
		List<SubSystem> subSystems = Arrays.asList(subsystem1, subsystem2, subsystem3);
		daq.setSubSystems(subSystems);
		daq.setLevelZeroState(levelZeroState);
		daq.setLastUpdate(lastUpdate);
		return daq;
	}

}
