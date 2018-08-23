package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;

/**
 *
 * @author Maciej Gladki
 */
public class RuFailedTest extends FlowchartCaseTestBase {

	/**
	 *
	 * Fri Jun 9 17:21:56 CEST 2017
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-06-09-17:21:55
	 * 
	 * Approved message by Remi: 'Ru failed' 17 RUs ([ru-c2e15-27-01.cms and 16 more]) are in failed state for an
	 * unidentified reason. The most often occurring ((1 times) error message is: Caught exception: exception::TCP
	 * 'Received a connection from 10.180.226.90:10107 while not accepting new connections' raised at
	 * connectionAcceptedEvent(/usr/local/src/xdaq/baseline14/trunk/daq/evb/include/evb/readoutunit/
	 * FerolConnectionManager.h:191)
	 * 
	 * Andre: RUs failed because they could not send superfragments over Infiniband because the event size was about 9
	 * MByte (a few seconds before the RU failed, not in this snapshot where triggers have stopped). FC2 should not fire
	 * because there was no FED with data corruption in this instance. note that also in the past, FlowchartCase6 fired
	 * on this test case but it was modified not to
	 * 
	 * REMI: the real reason is in the 2nd (ru-failed) bullet. I guess the 1st (fc5, fed-stuck) one shows up because we
	 * do not see the backpressure from DAQ on the BPIX FEDs.
	 */
	@Ignore // this is due to known issue with the testBase - enable it back after new TestBase merged
	@Test
	public void case1Test() throws URISyntaxException {

		DAQ snapshot = getSnapshot("1497021716430.smile");

		// Discussed issue #165, case 6
		assertSatisfiedLogicModules(snapshot, ruFailed, backpressureFromEventBuilding);


		// check the error message from ruFailed
		// 9 RUs had the same error message, one had a slightly different one
		final String expectedMostFrequentErrorMessage = "Caught exception: exception::I2O 'Failed to send super fragment to BU TID 137' raised at postFrames(/usr/local/src/xdaq/baseline14/trunk/daq/evb/include/evb/readoutunit/BUposter.h:344);\n\toriginated by exception::I2O 'Failed to send message after 11 retries to http://bu-c2d46-10-01.cms:11100/urn:xdaq-application:lid=51' raised at postMessage(/usr/local/src/xdaq/baseline14/trunk/daq/evb/include/evb/EvBApplication.h:837);\n\toriginated by xdaq::exception::Exception 'Failed to post frame' raised at po";
		final int expectedMostFrequentErrorCount = 9;

		// note that these are ALL rus which are in failed state,
		// not only those with the most abundant error message
		final String[] expectedRUs = { "ru-c2e13-23-01.cms", "ru-c2e13-15-01.cms", "ru-c2e13-14-01.cms",
				"ru-c2e13-17-01.cms", "ru-c2e13-16-01.cms", "ru-c2e13-13-01.cms", "ru-c2e12-13-01.cms",
				"ru-c2e13-24-01.cms", "ru-c2e13-39-01.cms", "ru-c2e13-19-01.cms" };

		// TODO: should we introduce named constants for the contextHandler keys ?
		assertEquals(new HashSet<>(Arrays.asList(expectedMostFrequentErrorMessage)),
				ruFailed.getContextHandler().getContext().get("MOSTFREQUENTERROR"));

		assertEquals(new HashSet<>(Arrays.asList(expectedMostFrequentErrorCount)),
				ruFailed.getContextHandler().getContext().get("MOSTFREQUENTERRORCOUNT"));

		assertEquals(new HashSet<>(Arrays.asList(expectedRUs.length)),
				ruFailed.getContextHandler().getContext().get("NUMFAILEDRUS"));

		assertEquals(new HashSet<>(Arrays.asList(expectedRUs)), ruFailed.getContextHandler().getContext().get("RU"));

		// ----------

	}

	@Test
	public void case2Test() throws URISyntaxException {

		// Fri May 19 15:28:49 CEST 2017
		DAQ snapshot = getSnapshot("1495200529063.smile");

		// FC2 should not fire because there was no FED
		// with data corruption in this instance

		assertSatisfiedLogicModules(snapshot, ruFailed);

		// check the error message from ruFailed. Note that in this case
		// all RUs have the same error message but with one part different
		// so for the moment this module reports only one of them
		final String expectedMostFrequentErrorMessagePattern = Pattern
				.quote("Caught exception: exception::TCP 'Received a connection from ") + "\\S+"
				+ Pattern.quote(
						" while not accepting new connections' raised at connectionAcceptedEvent(/usr/local/src/xdaq/baseline14/trunk/daq/evb/include/evb/readoutunit/FerolConnectionManager.h:191)");

		// note that these are ALL rus which are in failed state,
		// not only those with the most abundant error message
		final String[] expectedRUs = { "ru-c2e14-24-01.cms", "ru-c2e14-11-01.cms", "ru-c2e12-34-01.cms",
				"ru-c2e14-29-01.cms", "ru-c2e14-10-01.cms", "ru-c2e14-22-01.cms", "ru-c2e12-30-01.cms",
				"ru-c2e12-39-01.cms", "ru-c2e12-22-01.cms", "ru-c2e14-19-01.cms", "ru-c2e12-17-01.cms",
				"ru-c2e15-13-01.cms", "ru-c2e15-23-01.cms", "ru-c2e14-16-01.cms", "ru-c2e12-23-01.cms",
				"ru-c2e15-27-01.cms", "ru-c2e12-19-01.cms", };

		// note that there are no guarantees on which message we actually
		// get so we match with a pattern
		String mostFreqErrorMessage = ruFailed.getContextHandler().getContext().getContextEntryMap().get("MOSTFREQUENTERROR").getTextRepresentation();

		assertTrue(
				"most frequent error message did not match expected regex," + " got instead: " + mostFreqErrorMessage,
				Pattern.matches(expectedMostFrequentErrorMessagePattern, mostFreqErrorMessage));

		Integer actualMostFrequentErrorCount = ((ObjectContextEntry<Integer>)ruFailed.getContextHandler().getContext().getContextEntryMap().get("MOSTFREQUENTERRORCOUNT")).getObjectSet()
				.iterator().next();

		assertTrue("did not find at least one most abundant error message", actualMostFrequentErrorCount >= 1);

		assertEquals(new HashSet<>(Arrays.asList(expectedRUs.length)),
				ruFailed.getContextHandler().getContext().get("NUMFAILEDRUS"));

		assertEquals(new HashSet<>(Arrays.asList(expectedRUs)), ruFailed.getContextHandler().getContext().get("RU"));

		// ----------

	}

}