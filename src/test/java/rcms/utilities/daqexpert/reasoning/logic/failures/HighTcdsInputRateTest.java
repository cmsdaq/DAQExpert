package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TCDSGlobalInfo;
import rcms.utilities.daqaggregator.data.TCDSTriggerRates;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;

/**
 *
 * @author holzner
 */
public class HighTcdsInputRateTest
{

	@Test
	public void test01() throws URISyntaxException
	{
		DAQ snapshot = FlowchartCaseTestBase.getSnapshot("1506901411493.json.gz");

		// because this is an old snapshot which did not have the instantaneous
		// TCDS rates in it, we just copy them manually from the per lumisection
		// ones
		TCDSGlobalInfo tcdsInfo = snapshot.getTcdsGlobalInfo();
		TCDSTriggerRates instantTriggerRates = tcdsInfo.getTriggerRatesInstant();

		instantTriggerRates.setSup_trg_rate_beamactive_tt_values(tcdsInfo.getSup_trg_rate_beamactive_tt_values());
		instantTriggerRates.setSup_trg_rate_tt_values           (tcdsInfo.getSup_trg_rate_tt_values());
		instantTriggerRates.setTrg_rate_beamactive_tt_values    (tcdsInfo.getTrg_rate_beamactive_tt_values());
		instantTriggerRates.setTrg_rate_tt_values               (tcdsInfo.getTrg_rate_tt_values());
		instantTriggerRates.setSup_trg_rate_beamactive_total    (tcdsInfo.getSup_trg_rate_beamactive_total());
		instantTriggerRates.setSup_trg_rate_total               (tcdsInfo.getSup_trg_rate_total());
		instantTriggerRates.setTrg_rate_beamactive_total        (tcdsInfo.getTrg_rate_beamactive_total());
		instantTriggerRates.setTrg_rate_total                   (tcdsInfo.getTrg_rate_total());
		instantTriggerRates.setSectionNumber_rates              (tcdsInfo.getSectionNumber_rates());

		//-----

		Map<String, Boolean> results = new HashMap<>();

		results.put(StableBeams.class.getSimpleName(), true);

		// ensure that the VeryHighTcdsInputRate module fires
		HighTcdsInputRate module = new HighTcdsInputRate();

		// mock parameters
		Properties config = new Properties();
		config.put(Setting.EXPERT_TCDS_INPUT_RATE_HIGH.getKey(),     "100000");
		config.put(Setting.EXPERT_TCDS_INPUT_RATE_VERYHIGH.getKey(), "200000");
		module.parametrize(config);

		// run module to be tested
		boolean result = module.satisfied(snapshot, results);

		assertEquals(true, result);

		// TODO: check the detected trigger rate. We can't easily get this from
		// module.getContext().
		// We could either compare the string with get from
		// module.getContext().getContentWithContext("{{TCDS_TRIGGER_INPUT_RATE}}"));
		// add a getter for contextForCalculations in class Context
	}

}
