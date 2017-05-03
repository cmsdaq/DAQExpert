package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.SimpleLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Module which catches otherwise unidentified failures
 * which can then be looked for in the notification manager archive
 * 
 * @author holzner
 */
public class UnidentifiedFailure extends KnownFailure
{
	/** names of logic modules which inherit from KnownFailure
	 *  which we should look at. Note that we initialize this 
	 *  on demand only to avoid that this list depends on 
	 *  the order in which logic modules are added to LogicModuleRegistry
	 *  (otherwise we would have to ensure that UnidentifiedFailure is registered
	 *  after all others)
	 */
	private static final Set<String> knownFailureClasses = new HashSet<String>();
	
	public UnidentifiedFailure() {

		this.name = "Unidentified problem";

		this.description = "Reason for no trigger rate could not identified";

		/* Default action */
		this.action = new ConditionalAction("Make an ELOG entry");

	}
	
	private static Set<String> getKnownFailureClasses() {
		synchronized (knownFailureClasses) {
			
			if (knownFailureClasses.isEmpty()) {
				
				// find logic module classes which inherit from KnownFailure
				// by the time this executes all modules should be registered
				for (LogicModuleRegistry lmr : LogicModuleRegistry.values()) {
				
					LogicModule lm = lmr.getLogicModule(); 
					
					// exclude ourselves
					if (lm instanceof UnidentifiedFailure)
						continue;
					
					if (lm instanceof KnownFailure) {
						knownFailureClasses.add(lm.getClass().getSimpleName());
					}
						
				} // loop over all known logic modules
				
			} // if not yet filled
			
			// TODO: could use Collections.unmodifiableSet(..) here
			return knownFailureClasses;
		}
	}
	
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		
		// fire only if NoRateWhenExpected holds
		// (assume this module always has produced a result)
		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		// look for any module inheriting from KnownFailure which identified
		// a problem. We treat a non-existing result as 'not identified'
		for (String moduleName : getKnownFailureClasses()) {

			Boolean thisResult = results.get(moduleName);
			
			if (thisResult != null && thisResult)
				// found a logic module which identified the problem
				return false;
			
		} // loop over classes

		// no logic module found so far so we claim this problem
		return true;
		
	}
	
}
