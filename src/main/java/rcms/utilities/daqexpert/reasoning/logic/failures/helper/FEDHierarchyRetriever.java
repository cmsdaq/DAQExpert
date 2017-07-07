package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * FED hierarchy resolver.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FEDHierarchyRetriever {

	private static final Logger logger = Logger.getLogger(FEDHierarchyRetriever.class);

	/**
	 * Retrieves FED hierarchy from given partition. Note that the depth of
	 * hierarchy is maximum 1
	 * 
	 * @param partition
	 * @return Map of FEDs representing groups of hierarchies
	 */
	public static Map<FED, Set<FED>> getFEDHierarchy(TTCPartition partition) {

		Map<FED, Set<FED>> depende = new HashMap<>();
		Map<FED, Set<FED>> revertedDependencyTree = new HashMap<>();

		logger.debug("Listing all FEDs (" + partition.getFeds().size() + ") of partition " + partition.getName());
		for (FED fed : partition.getFeds()) {

			if (!(fed.isFrlMasked() && fed.isFmmMasked())) {

				Set<FED> depFeds = new HashSet<>();

				String compactDependentList = "[";

				if (fed.isHasTTS() && fed.isHasSLINK()) {
					// if FED has both SLINK and TTS than don't treat is
					// as a dependent FED - that's the case for all ECAL
					// FEDS
					compactDependentList += "ignored";
				} else {

					for (FED dep : fed.getDependentFeds()) {

						if (!(dep.isFrlMasked() && dep.isFmmMasked())) {

							compactDependentList += dep.getSrcIdExpected() + ", ";

							depFeds.add(dep);
						}
					}
				}
				compactDependentList += "]";
				depende.put(fed, depFeds);

				logger.debug("FED: " + fed.getSrcIdExpected() + ", deps: " + fed.getDependentFeds().size() + ": "
						+ compactDependentList);
			}
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
