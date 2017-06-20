package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Some utility functions used by the logic modules.
 */
public class LogicModuleHelper {

	private static final Logger logger = Logger.getLogger(LogicModuleHelper.class);

	/**
	 * @return the event counter of the reference FED (TCDS) or null if this
	 *         can't be found.
	 */
	public static Long getRefEventCounter(DAQ daq) {
		// find the EVM
		// TODO: we could cache this as long as the session id
		// does not change
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

	/**
	 * @return a list of FEDs which have sent fewer events than the TCDS.
	 *         Ignores those which are FRL masked or do not have an FRL
	 *         (pseudofeds).
	 *
	 *         This can be used to find the FEDs which block data taking because
	 *         they are not sending fragments anymore (if the trigger rate is
	 *         zero).
	 */
	public static List<FED> getFedsWithFewerFragments(DAQ daq) {

		List<FED> result = new ArrayList<>();

		Long refCounter = getRefEventCounter(daq);

		if (refCounter == null) {
			// we can't find the EVM, there is a problem
			return result;
		}

		for (FED fed : daq.getFeds()) {

			if (!fed.isHasSLINK()) {
				// pseudofed, can't block data taking by not sending triggers
				continue;
			}

			if (fed.isFrlMasked()) {
				// FED's slink is masked
				continue;
			}

			if (fed.getEventCounter() < refCounter) {
				// this FED is behind
				result.add(fed);
			}

		} // loop over FEDs

		return result;
	}

	/**
	 * TODO: if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
	 * 
	 * @param partition
	 * @return
	 */
	public static Map<FED, Set<FED>> getFEDHierarchy(TTCPartition partition) {

		Map<FED, Set<FED>> depende = new HashMap<>();
		Map<FED, Set<FED>> revertedDependencyTree = new HashMap<>();
		Set<FED> dependent = new HashSet<>();

		logger.debug("Listing all FEDs (" + partition.getFeds().size() + ") of partition " + partition.getName());
		for (FED fed : partition.getFeds()) {

			Set<FED> depFeds = new HashSet<>();

			String compactDependentList = "[";
			for (FED dep : fed.getDependentFeds()) {

				compactDependentList += dep.getSrcIdExpected() + ", ";
				dependent.add(dep);
				depFeds.add(dep);
			}
			compactDependentList += "]";
			depende.put(fed, depFeds);

			logger.debug("FED: " + fed.getSrcIdExpected() + ", deps: " + fed.getDependentFeds().size() + ": "
					+ compactDependentList);
		}

		Iterator<Entry<FED, Set<FED>>> i = depende.entrySet().iterator();
		while (i.hasNext()) {
			Entry<FED, Set<FED>> n = i.next();

			logger.debug("Reverting entry: " + n.getKey().getSrcIdExpected() + ":" + n.getValue());
			if (n.getValue() == null || n.getValue().size() == 0) {
				logger.trace("- putting single key");
				if (!revertedDependencyTree.containsKey(n.getKey())) {
					revertedDependencyTree.put(n.getKey(), new HashSet<FED>());
				}
			}

			for (FED dep : n.getValue()) {
				if (revertedDependencyTree.containsKey(dep)) {
					logger.trace("- reverting");
					revertedDependencyTree.get(dep).add(n.getKey());
				} else {
					logger.trace("- Initializing set with one element");
					revertedDependencyTree.put(dep, new HashSet<FED>(Arrays.asList(n.getKey())));
				}
			}
			logger.debug("Current: " + revertedDependencyTree);

		}

		return revertedDependencyTree;
	}

}
