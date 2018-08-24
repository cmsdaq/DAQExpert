package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import rcms.utilities.daqexpert.jobs.RecoveryRequest;
import rcms.utilities.daqexpert.jobs.RecoveryRequestBuilder;
import rcms.utilities.daqexpert.jobs.RecoveryStep;

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
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 1)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 3)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 5)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 7)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 8)));

		// TODO: check list of previous occ

		Assert.assertNull(lm.getContextHandler().getContext().get("PROBLEM-SUBSYSTEM"));
	}

	@Test
	public void forthOccurrenceSatisfiesTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8)));

		Set<String> problematicSubsystems = ((ObjectContextEntry<String>)lm.getContextHandler().getContext().getContextEntryMap().get("SUBSYSTEM_WITH_COUNTS")).getObjectSet() ;
		Assert.assertNotNull(problematicSubsystems);
		Assert.assertEquals(1, problematicSubsystems.size());
		Assert.assertEquals("[B 4 time(s), C 4 time(s)]", problematicSubsystems.iterator().next().toString());
	}

	@Test
	public void connectOccurrencesWithShortBreakTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14)));

	}

	@Test
	public void toggleOffAfterTimeThresholdTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 15)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 16)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 17)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 18)));
		// last timestamp of FIX is 14, with threshold 5, 19 should not pass
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 19)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 20)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 21)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 22)));
	}

	@Test
	public void doNotConnectOccurrencesWithLongerBreakTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 15)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 16)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 17)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 18)));
		// last timestamp of FIX is 14, with threshold 5, 19 should not pass
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 19)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 20)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 21)));
		// last FIX timestamps in last 100 are: 14, 12, 10... - it's more than 3
		// - any FIX before 114 will satisfy
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 22)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 23)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 24)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 25)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 26)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 27)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 28)));
	}

	@Test
	public void resetAfterPeriodTest() {
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 1)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 2)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 3)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 4)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 5)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 6)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 7)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 8)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 9)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 10)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 11)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 12)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 13)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 14)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 15)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 16)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 17)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(ELSE, 18)));
		// last timestamp of FIX is 14, with threshold 5, 19 should not pass
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 19)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 20)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 21)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 22)));
		// the last FIX was 14
		// period of 100 passed, next FIX should not satisfy, at least 3 will
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 114)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 115)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 116)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 117)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 118)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 119)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(FIX, 120)));
		Assert.assertEquals(false, lm.satisfied(generateSnapshot(ELSE, 121)));
		Assert.assertEquals(true, lm.satisfied(generateSnapshot(FIX, 122)));
	}

	/** configures the ContinouslySoftError module with thresholds used in/closer to
	 *  production such that the tests work on snapshots generated based
	 *  on behaviour seen in production.
	 */
	private void setLmProductionThresholds() {
		Properties p = new Properties();
		p.setProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_COUNT.getKey(), Integer.toString(3));
		p.setProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_PERIOD.getKey(), Integer.toString(600000));
		p.setProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_KEEP.getKey(), Integer.toString(15000));
		lm.parametrize(p);
	}


	/** Tests a case where two subsystems went several times into
	 *  fixing soft error state within the time window but only one
	 *  of them exceeded the threshold. The error message should only
	 *  contain the latter subsystem (see issue #175).
	 */
	@Test
	public void multipleSubsystemsTest() {
	
		// configure the logic module with the parameters used in production
		// (the ones used in before() are too short
		setLmProductionThresholds();

		List<DAQ> snapshots = generateMultipleSubsystemsSequence().makeSnapshots();
		
		for (DAQ snapshot : snapshots) {

			// clear any previous messages
			lm.getContextHandler().clearContext();

			// run the test
			boolean actualResult = lm.satisfied(snapshot);
			
			// module should fire only from a certain point in time on
			boolean expectedResult = (snapshot.getLastUpdate() >= 1525297407394L);
			
			Assert.assertEquals(expectedResult, actualResult);

			
			if (actualResult) {
				// module did fire, ensure that it complains only about CTPPS
				// but not about PIXEL

				// note that due to merging we can have messages == null
				// but still a true result from lm.satisfied() (TODO: is this wanted ?)
			  Set<String> messages = (Set<String>)(lm.getContextHandler().getContext().get("PROBLEM-SUBSYSTEM"));

				if (messages != null) {
					Assert.assertEquals(1, messages.size());
					Assert.assertTrue(messages.iterator().next().startsWith("CTPPS"));
				}


				System.out.println(lm.getDescriptionWithContext());
				//Assert.assertEquals("",lm.getDescriptionWithContext());
			}
		}

	}
	
	/** test to check that we get the expected message for CTPPS (including
	    action objects) */
	@Test
	public void checkCTPPSmessage() {
	
		// configure the logic module with the parameters used in production
		// (the ones used in before() are too short
		setLmProductionThresholds();

		List<DAQ> snapshots = generateMultipleSubsystemsSequence().makeSnapshots();
		
		for (DAQ snapshot : snapshots) {

			// clear any previous messages
			lm.getContextHandler().clearContext();

			if (! lm.satisfied(snapshot)) {
				continue;
			}
							
			// check that there is a recovery request
			RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder();
			RecoveryRequest recoveryRequests = recoveryRequestBuilder.buildRecoveryRequest(
							lm.getActionWithContextRawRecovery(),
							lm.getName(),
							lm.getDescriptionWithContext(),
							0L);
			
			assertEquals(1, recoveryRequests.getRecoverySteps().size());
			
			RecoveryStep recoveryStep = recoveryRequests.getRecoverySteps().iterator().next();
			
			// ensure that we have exactly one recovery step
			assertEquals(1, recoveryStep.getRedRecycle().size());
			
			// ensure that this is a red recycle
			assertEquals("CTPPS", recoveryStep.getRedRecycle().iterator().next());
			
			// just check the first occurrence. Some of the later occurrences
			// do not have a subsystem due to the merging mechanism.
			return;
		}
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

	/** generates a sequence found in production where two subsystems
	 * had multiple fixing soft error transitions but only one
	 * of them crossed the threshold.
	 */
	private SubsystemStateSequenceGenerator generateMultipleSubsystemsSequence() {
		
		SubsystemStateSequenceGenerator generator = new SubsystemStateSequenceGenerator();
		
		generator.addPointInTime(1525297072570L);
		
		generator.addNewState(1525297074958L, "CTPPS", "Running");
		generator.addNewState(1525297074958L, "PIXEL", "RunningSoftErrorDetected");
		generator.addNewState(1525297074958L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297080113L, "PIXEL", "FixingSoftError");

		generator.addNewState(1525297083378L, "PIXEL", "Running");

		generator.addNewState(1525297086415L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297119886L, "PIXEL", "FixingSoftError");
		generator.addNewState(1525297119886L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297127837L, "PIXEL", "Running");

		generator.addNewState(1525297130307L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297162102L, "PIXEL", "RunningSoftErrorDetected");
		generator.addNewState(1525297162102L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297164718L, "PIXEL", "FixingSoftError");

		generator.addNewState(1525297169807L, "PIXEL", "Running");

		generator.addNewState(1525297177621L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297366508L, "CTPPS", "FixingSoftError");
		generator.addNewState(1525297366508L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297371563L, "CTPPS", "Running");

		generator.addNewState(1525297374058L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297376721L, "CTPPS", "RunningSoftErrorDetected");
		generator.addNewState(1525297376721L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297379313L, "CTPPS", "FixingSoftError");

		generator.addNewState(1525297382180L, "CTPPS", "Running");

		generator.addNewState(1525297387419L, "CTPPS", "FixingSoftError");

		generator.addNewState(1525297392860L, "CTPPS", "Running");
		generator.addNewState(1525297392860L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297396337L, "CTPPS", "RunningSoftErrorDetected");
		generator.addNewState(1525297396337L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297399136L, "CTPPS", "FixingSoftError");

		generator.addNewState(1525297401848L, "CTPPS", "Running");

		generator.addNewState(1525297404741L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		// fourth time CTPPS goes into FixingSoftError or RunningSoftErrorDetected
		// should fire according to the currently implemented logic
		// (which also counts transitions between RunningSoftErrorDetected and
		// FixingSoftError)
		generator.addNewState(1525297407394L, "CTPPS", "RunningSoftErrorDetected");
		generator.addNewState(1525297407394L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297410082L, "CTPPS", "FixingSoftError");

		generator.addNewState(1525297413310L, "CTPPS", "Running");
		generator.addNewState(1525297413310L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297416491L, "CTPPS", "RunningSoftErrorDetected");
		generator.addNewState(1525297416491L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297419166L, "CTPPS", "FixingSoftError");

		generator.addNewState(1525297422117L, "CTPPS", "Running");

		generator.addNewState(1525297424856L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297427474L, "CTPPS", "FixingSoftError");
		generator.addNewState(1525297427474L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297433457L, "CTPPS", "Running");
		generator.addNewState(1525297433457L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		generator.addNewState(1525297436424L, "CTPPS", "RunningSoftErrorDetected");
		generator.addNewState(1525297436424L, SubsystemStateSequenceGenerator.LEVELZERO, "FixingSoftError");

		generator.addNewState(1525297439432L, "CTPPS", "FixingSoftError");

		generator.addNewState(1525297442171L, "CTPPS", "Running");

		generator.addNewState(1525297444861L, SubsystemStateSequenceGenerator.LEVELZERO, "Running");

		return generator;
	}

}
