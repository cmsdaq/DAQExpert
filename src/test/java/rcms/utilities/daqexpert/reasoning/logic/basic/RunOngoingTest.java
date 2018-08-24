package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;

public class RunOngoingTest {

	String tcdsNotRunning = "HALTED";
	String tcdsRunning = "RUNNING";
	String tcdsNeutral = "ERROR";

	String lzNotRunning = "INITIALIZING";
	String lzRunning = "RUNNING";
	String lzNeutral = "ERROR";

	@Test
	public void toggleOnFromTcdsSimpleTest() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(false, ro.satisfied(getMockDAQ(lzNeutral, tcdsNeutral)));
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzNeutral, tcdsRunning)));
	}

	@Test
	public void toggleOnFromLevelZeroSimpleTest() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(false, ro.satisfied(getMockDAQ(lzNeutral, tcdsNeutral)));
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzRunning, tcdsNeutral)));
	}

	@Test
	public void toggleOffFromTcdsSimpleTest() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzNeutral, tcdsRunning)));
		Assert.assertEquals(false, ro.satisfied(getMockDAQ(lzNeutral, tcdsNotRunning)));
	}

	@Test
	public void toggleOffFromLevelZeroSimpleTest() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzRunning, tcdsNeutral)));
		Assert.assertEquals(false, ro.satisfied(getMockDAQ(lzNotRunning, tcdsNeutral)));
	}

	@Test
	public void keepOnFromTcdsSimpleTest() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzNeutral, tcdsRunning)));
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzNeutral, tcdsNeutral)));
	}

	@Test
	public void keepOnFromLevelZeroSimpleTest() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzRunning, tcdsNeutral)));
		Assert.assertEquals(true, ro.satisfied(getMockDAQ(lzNeutral, tcdsNeutral)));
	}

	@Test
	public void bothNotRunning() {
		RunOngoing ro = new RunOngoing();
		Assert.assertEquals(false, ro.satisfied(getMockDAQ(lzNotRunning, tcdsNotRunning)));
	}

	private DAQ getMockDAQ(String levelZeroState, String tcdsState) {

		DAQ daq = new DAQ();
		daq.setLevelZeroState(levelZeroState);
		daq.setSubSystems(new ArrayList<SubSystem>());
		SubSystem ss = new SubSystem();
		ss.setName("TCDS");
		ss.setStatus(tcdsState);
		daq.getSubSystems().add(ss);

		return daq;
	}

}
