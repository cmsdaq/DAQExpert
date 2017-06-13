package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import static rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase.getSnapshot;

/**
 *
 * @author Maciej Gladki
 */
public class RuFailedTest extends FlowchartCaseTestBase {

	@Test
	public void case1Test() throws URISyntaxException {

		// RUs failed because they could not send superfragments
		// over Infiniband because the event size was about 9 MByte
		// (a few seconds before the RU failed, not in this snapshot
		// where triggers have stopped)
		//
		// note that also in the past, FlowchartCase6 fired on this test
		// case but it was modified not to
		// 
		// Fri Jun  9 17:21:56 CEST 2017
		DAQ snapshot = getSnapshot("1497021716430.smile");

		assertEqualsAndUpdateResults(false, fc1,snapshot);

		// FC2 should not fire because there was no FED
		// with data corruption in this instance
		assertEqualsAndUpdateResults(false, fc2,snapshot);

		assertEqualsAndUpdateResults(true,  ruFailed,snapshot);

		// check the error message from ruFailed
		// 9 RUs had the same error message, one had a slightly different one
		final String expectedMostFrequentErrorMessage =
						"Caught exception: exception::I2O 'Failed to send super fragment to BU TID 137' raised at postFrames(/usr/local/src/xdaq/baseline14/trunk/daq/evb/include/evb/readoutunit/BUposter.h:344);\n\toriginated by exception::I2O 'Failed to send message after 11 retries to http://bu-c2d46-10-01.cms:11100/urn:xdaq-application:lid=51' raised at postMessage(/usr/local/src/xdaq/baseline14/trunk/daq/evb/include/evb/EvBApplication.h:837);\n\toriginated by xdaq::exception::Exception 'Failed to post frame' raised at po";
		final int expectedMostFrequentErrorCount = 9;

		// note that these are ALL rus which are in failed state,
		// not only those with the most abundant error message
		final String[] expectedRUs = {
			"ru-c2e13-23-01.cms", "ru-c2e13-15-01.cms",
			"ru-c2e13-14-01.cms", "ru-c2e13-17-01.cms",
			"ru-c2e13-16-01.cms", "ru-c2e13-13-01.cms",
			"ru-c2e12-13-01.cms", "ru-c2e13-24-01.cms",
			"ru-c2e13-39-01.cms", "ru-c2e13-19-01.cms"
		};
		
		// TODO: should we introduce named constants for the context keys ?
		assertEquals(new HashSet<>(Arrays.asList(expectedMostFrequentErrorMessage)),
		  ruFailed.getContext().getContext().get("MOSTFREQUENTERROR"));

		assertEquals(new HashSet<>(Arrays.asList(expectedMostFrequentErrorCount)),
			   			  ruFailed.getContext().getContext().get("MOSTFREQUENTERRORCOUNT"));

		assertEquals(new HashSet<>(Arrays.asList(expectedRUs.length)),
			   			  ruFailed.getContext().getContext().get("NUMFAILEDRUS"));

		assertEquals(new HashSet<>(Arrays.asList(expectedRUs)),
			   			  ruFailed.getContext().getContext().get("RU"));

		//----------

		assertEqualsAndUpdateResults(false, fc3,snapshot);

		// new subcases of old flowchart case 4
		assertEqualsAndUpdateResults(false, piDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, piProblem,snapshot);
		assertEqualsAndUpdateResults(false, fedDisconnected,snapshot);
		assertEqualsAndUpdateResults(false, fmmProblem,snapshot);


		assertEqualsAndUpdateResults(false, fc5,snapshot);
		assertEqualsAndUpdateResults(true, fc6,snapshot);
		assertEquals(false, unidentified.satisfied(snapshot, results));


	}

}