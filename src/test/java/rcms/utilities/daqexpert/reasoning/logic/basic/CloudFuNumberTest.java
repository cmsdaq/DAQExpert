package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.Setting;

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
	private void doChecks(CloudFuNumber instance, String snapshotFname,
					boolean expectedResult,
					boolean expectedResultBeforeHoldOff,
					String expectedMessage) throws URISyntaxException {

		DAQ daq = getSnapshot(snapshotFname);

		Map<String, Boolean> results = new HashMap<String, Boolean>();

		boolean result = instance.satisfied(daq, results);
		assertEquals("result after taking into account holdoff for snapshot " + snapshotFname,
						expectedResult, result);

		// check the 'raw' result before applying the holdoff timer
		boolean resultBeforeHoldOff = ! instance.cloudCanBeOn(daq);  //instance.getHoldOffTimer().getInput();
		assertEquals("result before taking into account holdoff for snapshot " + snapshotFname,
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
	public void test01() throws URISyntaxException
	{
		CloudFuNumber instance = this.makeInstance();

		// last snapshot of fill 6175 in 'PREPARE RAMP'
		doChecks(instance, "1504781255663.json.gz",
						false,  // expected output result
						false,  // expected result before holdoff
						null
						);

		// snapshot immediately afterwards, first snapshot of this fill
		// in non-cloud LHC beams mode ('RAMP')
		//
		// (2415 msecs after previous snapshot according to the file name)
		doChecks(instance, "1504781258078.json.gz",
						false,  // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						null
						);

		// last snapshot before the holdoff time expires
		// (time difference is 1798.087 seconds)
		doChecks(instance, "1504783056165.json.gz",
						false,  // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						null
						);

		// next snapshot immediately afterwards, holdoff time expired for the first time
		doChecks(instance, "1504783058627.json.gz",
						true,  // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						"Fraction of FUs in cloud mode is <strong>49.5%</strong>, the threshold is 3.0%"
						);

		// last snapshot in BEAM DUMP mode (but had many FUs in cloud mode
		// which should not happen)
		doChecks(instance, "1504825569846.json.gz",
						true,   // expected output result with holdoff
						true,   // module should fire if there was no holdoff
						"Fraction of FUs in cloud mode is <strong>49.5%</strong>, the threshold is 3.0%"
						);

		// first snapshot in 'RAMP DOWN' mode, cloud can be on again
		doChecks(instance, "1504825571948.json.gz",
						false,   // expected output result with holdoff
						false,   // expected raw result
						null
						);

	}

}
