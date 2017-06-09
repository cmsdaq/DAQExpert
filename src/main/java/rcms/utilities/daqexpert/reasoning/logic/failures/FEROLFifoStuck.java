package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Logic module identifying cases when the FIFO in the FEROL40 (or FEROL) got 
 * stuck. This is actually similar to FlowChartCase6 but in this case
 * the number of events seen on the FEROL (at input) is the same as everybody
 * else should have seen.
 * 
 */
public class FEROLFifoStuck extends KnownFailure {

	public FEROLFifoStuck() {
		this.name = "FEROL/FEROL40 FIFO stuck";

		this.description = "FEROL of FED {{FEDID}} stopped sending fragments " 
				+ "its RU. This is likely a bug in the FEROL/FEROL40 firmware."
			;

		this.action = new SimpleAction("Make a dump of the FEROL/FEROL40 registers: "
				+ "go to {{FRLFULLURL}}, click on and click on \"Register Dump\".",

				"write an elog entry with the title "
				+ "\"Dump of FEROL40 with FED Id {{FEDID}} when blocking the run\" .",
						
				"Then stop the run and green recylce DAQ to continue data taking.",

				"See https://twiki.cern.ch/twiki/bin/viewauth/CMS/ShiftNews#Short_term_special_instructions for more information."

		);

	}

	private static final Logger logger = Logger.getLogger(FEROLFifoStuck.class);

	/** @return the event counter of the reference FED (TCDS) or null
	    if this can't be found. */
	private Long getRefEventCounter(DAQ daq) {
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
	
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		String daqstate = daq.getDaqState();

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {
			
			Long eventCounterRef = getRefEventCounter(daq);
			
			if (eventCounterRef != null) {

				// loop over FEDs
				for (FED fed : daq.getFeds()) {
				
					if (fed.isFrlMasked())
						continue;
					
					if (! fed.isRuFedWithoutFragments())
						continue;

					// RU has not received fragments from this fed

					// RU must not be in failed state (this happens
					// e.g. when the RU received a corrupted fragment
					// (potentially from some other FED in the same fedbuilder)
					RU ru = fed.getRu();

					// RU must have incomplete superfragments (this assumes that
					// the bug does not happen on all FEROLs attached to this RU
					// at the same time)
					if (ru.getIncompleteSuperFragmentCount() == 0)
						continue;

					// require that all FEDs in this subfedbuilder
					// have the same triggers on the FEROL
					SubFEDBuilder subFedBuilder = fed.getFrl().getSubFedbuilder();

					if (subFedBuilder.getMinTrig() < subFedBuilder.getMaxTrig())
						// not all FEROLs have the same number of triggers
						// this is not the problem we are looking for here
						continue;

					// RU must not be in Failed state
					if ("Failed".equalsIgnoreCase(ru.getStateName()))
						continue;

					if (fed.getEventCounter() >= eventCounterRef) {
						
						// but the FRL has received the same amount of fragments
						// like the TCDS

						// we found a FRL with this problem
						FRL frl = fed.getFrl();

						context.register("FEDID", fed.getSrcIdExpected());
						context.register("FRLPC", frl.getFrlPc().getHostname());
						context.register("FRLIO", fed.getFrlIO());

						// this is only the URL of the XDAQ process, not of the
						// FRL instance
						context.register("FRLURL", frl.getUrl());

						// LID of the instance of FEROL/FEROL40 controller controlling
						// this FED
						final int lid = 100 + frl.getGeoSlot();
						context.register("FRLLID", lid);

						// register also the full URL for the dump button
						String fullURL = frl.getUrl() + "/urn:xdaq-application:lid=" + lid + "/expertDebugPage";
						context.register("FRLFULLURL", fullURL);

						result = true;
					} // if problem in this FRL
				
				} // loop over FEDs
				
			} // if reference event counter found
		
		} // if not runblocked
		
		return result;
	}

}
