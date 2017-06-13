package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import java.util.Set;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;

/**
 *  Some utility functions used by the logic modules.
 */
public class LogicModuleHelper
{

	/** @return the event counter of the reference FED (TCDS) or null
	    if this can't be found. */
	public static Long getRefEventCounter(DAQ daq) {
		// find the EVM
		// TODO: we could cache this as long as the session id
		//       does not change
		RU evm = daq.getEVM();

		if (evm == null)
			return null;

		// find the FED(s) associated to the EVM
		// note that we want to compare the event counter on the FEROL
		// (which is reported in the FED object)
		// to the event counter on the TCDS FEROL, NOT to the eventCount
		// of the EVM which can be different (typically lower)
		Set<FED> feds = evm.getFEDs(false);

		if (feds.isEmpty())
			// no (unmasked) FED found
			return null;

		// just get the first FED of this RU
		FED refFED = feds.iterator().next();

		return refFED.getEventCounter();
	}

}
