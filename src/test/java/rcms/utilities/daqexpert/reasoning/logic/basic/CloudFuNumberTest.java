package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;
import rcms.utilities.daqexpert.reasoning.logic.failures.RateTooHigh;

/**
 *
 * @author holzner
 */
public class CloudFuNumberTest
{

	private CloudFuNumber makeInstance() {
		CloudFuNumber result = new CloudFuNumber();

		// mock properties
		Properties properties = new Properties();
		properties.setProperty(Setting.EXPERT_LOGIC_CLOUDFUNUMBER_THRESHOLD_TOTAL_FRACTION.getKey(), "0.03");
		properties.setProperty(Setting.EXPERT_LOGIC_CLOUDFUNUMBER_HOLDOFF_PERIOD.getKey(), "1800000");

		result.parametrize(properties);

		return result;
	}

	@Test
	public void criticalBeamModeTest() throws URISyntaxException
	{
		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1510706999513.json.gz");
		Map<String, Output> results = new HashMap<>();
		CloudFuNumber module = makeInstance();
		assertTrue(module.satisfied(snapshot, results));
	}


	@Test
	public void generatedSnapshotSequenceTest(){

		Map<String, Output> results = new HashMap<>();
		CloudFuNumber module = makeInstance();

		/* Do not fire in allowed LHC beam modes */
		assertFalse(module.satisfied(generateSnapshot(LHCBeamMode.PREPARE_RAMP, 50, 50, 1), results));
		assertFalse(module.satisfied(generateSnapshot(LHCBeamMode.PREPARE_RAMP, 50, 50, 45), results));
		assertFalse(module.satisfied(generateSnapshot(LHCBeamMode.PREPARE_RAMP, 50, 50, 90), results));

		/* once in disallowed state count holdoff period and fire */
		assertFalse(module.satisfied(generateSnapshot(LHCBeamMode.RAMP, 50, 50, 100), results));
		assertTrue(module.satisfied(generateSnapshot(LHCBeamMode.RAMP, 50, 50, 130), results));

		/* turn off when back into allowed LHC beam mode and don't fire*/
		assertFalse(module.satisfied(generateSnapshot(LHCBeamMode.PREPARE_RAMP, 50, 50, 131), results));

		/* ignore holdoff when in critical state and fire */
		assertTrue(module.satisfied(generateSnapshot(LHCBeamMode.STABLE_BEAMS, 50, 50, 132), results));

	}

	public static DAQ generateSnapshot(LHCBeamMode beamMode, int cloudFus, int hltFus, int minutes){
		DAQ daq = new DAQ();
		BUSummary buSummary = new BUSummary();

		buSummary.setNumFUsCloud(cloudFus);
		buSummary.setNumFUsHLT(hltFus);

		daq.setLhcBeamMode(beamMode.getCode());
		daq.setLastUpdate(minutes*1000L*60L);
		daq.setBuSummary(buSummary);

		return daq;
	}


	/**
	 * method to load and deserialize a snapshot given a file name
	 *
	 * similar to FlowchartCaseTestBase.getSnapshot() but with
	 * a different base directory.
	 */
	public static DAQ getSnapshot(String fname) throws URISyntaxException {

		StructureSerializer serializer = new StructureSerializer();

		URL url = Deadtime.class.getResource(fname);

		File file = new File(url.toURI());

		return serializer.deserialize(file.getAbsolutePath());
	}

	/** @param expectedResult is the result which should be returned
	 *    by the module, taking into account the holdoff time
	 *
	 * @param expectedResultBeforeHoldOff is the expected 'raw' result
	 *    before applying the holdoff timer
	 */
	private void doChecks(CloudFuNumber instance, DAQ daq,
					boolean expectedResult,
					boolean expectedResultBeforeHoldOff,
					String expectedMessage) throws URISyntaxException {


		Map<String, Output> results = new HashMap<>();

		boolean result = instance.satisfied(daq, results);
		assertEquals("result after taking into account holdoff for snapshot " + daq.getLastUpdate(),
						expectedResult, result);

		// check the 'raw' result before applying the holdoff timer
		boolean resultBeforeHoldOff = ! instance.cloudCanBeOn(daq);  //instance.getHoldOffTimer().getInput();
		assertEquals("result before taking into account holdoff for snapshot " + daq.getLastUpdate(),
						expectedResultBeforeHoldOff, resultBeforeHoldOff);

		// check that the message matches but only if satisfied() is
		// expected to be true (at this point in the code, also the actual
		// result is true)
		//
		// (the expected string may have to be updated when the visualization
		// code changes ?)
		if (expectedResult) {
			assertEquals("description message",
							expectedMessage, instance.getDescriptionWithContext()
							);
		}
	}

	/**
	 *  sequence of snapshots
	 */
	@Test
	public void realSnapshotSequenceTest() throws URISyntaxException
	{
		CloudFuNumber instance = this.makeInstance();

		// last snapshot of fill 6175 in 'PREPARE RAMP'
		doChecks(instance, getSnapshot("1504781255663.json.gz"),
						false,  // expected output result
						false,  // expected result before holdoff
						null
						);

		// snapshot immediately afterwards, first snapshot of this fill
		// in non-cloud LHC beams mode ('RAMP')
		//
		// (2415 msecs after previous snapshot according to the file name)
		doChecks(instance, getSnapshot("1504781258078.json.gz"),
						false,  // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						null
						);

		// last snapshot before the holdoff time expires
		// (time difference is 1798.087 seconds)
		// Beam mode is SQUEEZE - ignore holdoff fire LM
		doChecks(instance, getSnapshot("1504783056165.json.gz"),
						true,  // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						"Fraction of FUs in cloud mode is <strong>49.5%</strong>, the threshold is 3.0%"
						);

		// next snapshot immediately afterwards, holdoff time expired for the first time
		doChecks(instance, getSnapshot("1504783058627.json.gz"),
						true,  // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						"Fraction of FUs in cloud mode is <strong>49.5%</strong>, the threshold is 3.0%"
						);

		// last snapshot in BEAM DUMP mode (but had many FUs in cloud mode
		// which should not happen)
		doChecks(instance, getSnapshot("1504825569846.json.gz"),
						true,   // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						"Fraction of FUs in cloud mode is <strong>49.5%</strong>, the threshold is 3.0%"
						);

		// first snapshot in 'RAMP DOWN' mode, cloud can be on again
		doChecks(instance, getSnapshot("1504825571948.json.gz"),
						false,   // expected output result with holdoff
						false,   // expected raw result
						null
						);

	}

}
