package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import java.util.Map;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class HLTProblem extends BackpressureAnalyzer {

	public HLTProblem() {
		this.name = "HLT problem";

		this.description = "There are no requests on the RUs. No BUs are in enabled state. " +
				"Some FUs are quarantined {{FUS-QUARANTINED-PERCENTAGE}}, " +
				"(this means that CMSSW crashed already 5 times). "
				+ "This causes backpressure at FED {{AFFECTED-FED}} in partition {{AFFECTED-TTCP}} of {{AFFECTED-SUBSYSTEM}}";

		this.action = new SimpleAction("Call the HLT DOC and tell him/her that HLT processes crash. " +
				"This is most likely a problem in the L1 or HLT menu. " +
				"Another cause could be that the Frontier/DB service failed. ",
				"Please also call the DAQ DOC, as he has to clean up the F3 farm before the next run can start.");

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		boolean result = false;
		assignPriority(results);

		Subcase backpressureRootCase = detectBackpressure(daq);
		if (backpressureRootCase == Subcase.HltProblem) {
			result = true;
		}
		return result;
	}

}
