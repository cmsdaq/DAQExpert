/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.TestSnapshotBuilder;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.basic.RunOngoing;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author mgl
 */
public class HltOutputBandwidthTooHighTest
{

	@Test
	public void test01() throws URISyntaxException
	{
		Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(),"4.5");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(),"6.0");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_RUNONGOING_HOLDOFF_PERIOD.getKey(), "0");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_SELF_HOLDOFF_PERIOD.getKey(), "0");

		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1507212269717.json");
		KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
		((Parameterizable)hltOutputBandwidthTooHigh).parametrize(properties);
		Map<String, Output> results = new HashMap<>();
		results.put(StableBeams.class.getSimpleName(), new Output(true));
		results.put(BackpressureFromHlt.class.getSimpleName(), new Output(false));
		results.put(RunOngoing.class.getSimpleName(), new Output(true));
		Assert.assertTrue(hltOutputBandwidthTooHigh.satisfied(snapshot,results));
		Assert.assertEquals("The HLT output bandwidth is <strong>4.7GB/s</strong> which is above the threshold of 4.5 GB/s at which delays to Rate Monitoring and Express streams can appear. DQM files may get truncated resulting in lower statistics. This mode of operation may be normal for special runs if experts are monitoring.",hltOutputBandwidthTooHigh.getDescriptionWithContext());
	}

	@Ignore // no longer using notes to indicate that other problem is active. Will now use causality graph (affected nodes will be displayed)
	@Test
	public void testWithAdditionalNote() throws URISyntaxException
	{
		Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(),"4.5");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(),"6.0");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_RUNONGOING_HOLDOFF_PERIOD.getKey(), "0");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_SELF_HOLDOFF_PERIOD.getKey(), "0");

		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1507212269717.json");
		KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
		((Parameterizable)hltOutputBandwidthTooHigh).parametrize(properties);
		Map<String, Output> results = new HashMap<>();
		results.put(StableBeams.class.getSimpleName(), new Output(true));
		results.put(BackpressureFromHlt.class.getSimpleName(), new Output(true));
		Assert.assertTrue(hltOutputBandwidthTooHigh.satisfied(snapshot,results));
		Assert.assertEquals("The HLT output bandwidth is <strong>4.7GB/s</strong> which is above the threshold of 4.5 GB/s at which delays to Rate Monitoring and Express streams can appear. DQM files may get truncated resulting in lower statistics. This mode of operation may be normal for special runs if experts are monitoring. <strong>Note that there is also backpressure from HLT.</strong>",hltOutputBandwidthTooHigh.getDescriptionWithContext());
	}

	/**
	 * sequence obtained and simplified from snapshots
	 * 2018-08-08 12:15:00 to 12:22:00
	 */
	private List<DAQ> makeStartOfRunHoldOffSequence() throws IOException {

		List<DAQ> snapshots = new ArrayList<>();
		TestSnapshotBuilder sb = new TestSnapshotBuilder();

		// 2018-08-08 12:15:00
		sb.setLastUpdate(1533723300424L);
		sb.setItem("/daqState", "Configured");
		sb.setItem("/levelZeroState", "Undefined");
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 0.0);
		snapshots.add(sb.build());

		// 2018-08-08 12:15:16, 16.535 seconds after first snapshot
		sb.setLastUpdate(1533723316959L);
		sb.setItem("/levelZeroState", "Configured");
		snapshots.add(sb.build());

		// 2018-08-08 12:15:19, 19.430 seconds after first snapshot
		sb.setLastUpdate(1533723319854L);
		sb.setItem("/levelZeroState", "Starting");
		snapshots.add(sb.build());

		// 2018-08-08 12:15:36, 36.353 seconds after first snapshot
		sb.setLastUpdate(1533723336777L);
		sb.setItem("/daqState", "Starting");
		snapshots.add(sb.build());

		// 2018-08-08 12:16:22, 1 minutes and 21.695 seconds after first snapshot
		sb.setLastUpdate(1533723382119L);
		sb.setItem("/daqState", "Running");
		sb.setItem("/levelZeroState", "Running");
		snapshots.add(sb.build());

		// 2018-08-08 12:19:00, 4 minutes and 0.109 seconds after first snapshot
		sb.setLastUpdate(1533723540533L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 6.73531093951);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:11, 5 minutes and 10.859 seconds after first snapshot
		sb.setLastUpdate(1533723611283L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 2017.12614882);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:13, 5 minutes and 13.385 seconds after first snapshot
		sb.setLastUpdate(1533723613809L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 3646.66760558);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:16, 5 minutes and 16.140 seconds after first snapshot
		sb.setLastUpdate(1533723616564L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 4096.3551133);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:20, 5 minutes and 19.638 seconds after first snapshot
		sb.setLastUpdate(1533723620062L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 4881.05783007);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:33, 5 minutes and 33.193 seconds after first snapshot
		sb.setLastUpdate(1533723633617L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 4746.80991253);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:36, 5 minutes and 36.182 seconds after first snapshot
		sb.setLastUpdate(1533723636606L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 4728.72592523);
		snapshots.add(sb.build());

		// 2018-08-08 12:20:39, 5 minutes and 39.574 seconds after first snapshot
		sb.setLastUpdate(1533723639998L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 4102.90240888);
		snapshots.add(sb.build());

		// 2018-08-08 12:21:22, 6 minutes and 21.672 seconds after first snapshot
		sb.setLastUpdate(1533723682096L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 3802.72206542);
		snapshots.add(sb.build());

		// 2018-08-08 12:21:24, 6 minutes and 23.874 seconds after first snapshot
		sb.setLastUpdate(1533723684298L);
		sb.setItem("/buSummary/fuOutputBandwidthInMB", 3792.46728104);
		snapshots.add(sb.build());

		return snapshots;
	}


	/**
	 * tests a case which occurred in production: insists that the too high bandwidth
	 * is tolerated because it is too early after the beginning of the run.
	 * Either with or without holdoff.
	 *
	 * @param expectAtLeastOne iff true, expect the condition to be satisfied at least once for the snapshots
	 */
	private void testStartOfRunHoldOff01helper(long beginningOfRunHoldoff,
	                                           long conditionHoldOff,
	                                           boolean expectAtLeastOne) throws IOException {

		Logger.getLogger(HltOutputBandwidthTooHigh.class).setLevel(Level.INFO);

		List<DAQ> snapshots = makeStartOfRunHoldOffSequence();

		// the class under test
		KnownFailure hltOutputBandwidthTooHigh = new HltOutputBandwidthTooHigh();
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(),"4.5");
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(),"6.0");

		// these values were effectively zero before the holdoff logic was introduced
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_RUNONGOING_HOLDOFF_PERIOD.getKey(), "" + beginningOfRunHoldoff);
		properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_SELF_HOLDOFF_PERIOD.getKey(), "" + conditionHoldOff);

		((Parameterizable)hltOutputBandwidthTooHigh).parametrize(properties);

		RunOngoing runOngoing = new RunOngoing();

		Map<String, Output> results = new HashMap<>();
		results.put(StableBeams.class.getSimpleName(), new Output(true));

		long firstSnapshotTime = snapshots.get(0).getLastUpdate();

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		boolean atLeastOneSatisfied = false;

		for (DAQ snapshot : snapshots) {

			// assume TCDS is running whenever DAQ was running (which is actually the true
			// for this test case)
			SubSystem tcds = new SubSystem();
			tcds.setName("TCDS");
			tcds.setStatus(snapshot.getDaqState());
			snapshot.setSubSystems(new ArrayList<>());
			snapshot.getSubSystems().add(tcds);

			boolean runOngoingResult = runOngoing.satisfied(snapshot, results);
			results.put(runOngoing.getClass().getSimpleName(), new Output(runOngoingResult));

			boolean satisfied = hltOutputBandwidthTooHigh.satisfied(snapshot, results);

			atLeastOneSatisfied |= satisfied;

			if (!expectAtLeastOne) {
				// this testcase should never satisfy with the new holdoff logic for
				// the beginning of the run
				assertFalse("snapshot unexpectedly satisfied at " + df.format(new Date(snapshot.getLastUpdate())),
								satisfied);
			}
		}

		if (expectAtLeastOne) {
			assertTrue("none of the snapshots satisfied the condition", atLeastOneSatisfied);
		}
	}


	@Test
	public void testStartOfRunHoldOff01() throws IOException {

		// ensure that with the classic settings (short holdoffs), the condition should
		// fire at least once
		testStartOfRunHoldOff01helper(1000, 1000, true);

		// ensure that with new long holdoff settings the condition never fires for these
		// snapshots
		testStartOfRunHoldOff01helper(300000, 120000, false);
	}
}