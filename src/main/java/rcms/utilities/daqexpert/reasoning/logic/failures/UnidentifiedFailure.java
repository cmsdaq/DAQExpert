package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ActionLogicModule;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;

/**
 * Module which catches otherwise unidentified failures which can then be looked for in the notification manager
 * archive
 *
 * @author holzner
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class UnidentifiedFailure extends ActionLogicModule {
    /**
     * names of logic modules which inherit from KnownFailure which we should look at. Note that we initialize this on
     * demand only to avoid that this list depends on the order in which logic modules are added to LogicModuleRegistry
     * (otherwise we would have to ensure that UnidentifiedFailure is registered after all others)
     */
    protected Set<LogicModule> knownFailureClasses;

    public UnidentifiedFailure() {

        this.name = "Unidentified problem";

        this.description = "Trigger rate is zero. The problem could not be identified.";

        this.action = new SimpleAction("Recover the system according to instructions from shifter bulletin board",
                "Make an ELOG entry");


    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        // fire only if NoRateWhenExpected holds
        // (assume this module always has produced a result)
        if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
            return false;

        assignPriority(results);

        // look for any module inheriting from KnownFailure which identified
        // a problem. We treat a non-existing result as 'not identified'
        for (LogicModule module : knownFailureClasses) {

            Boolean thisResult = results.get(module.getClass().getSimpleName()).getResult();

            if (thisResult != null && thisResult)
                // found a logic module which identified the problem
                return false;

        } // loop over classes

        // no logic module found so far so we claim this problem
        return true;

    }

    @Override
    public void declareRelations() {
        Arrays.stream(LogicModuleRegistry.values())
                .filter(l->l.getLogicModule() != null)
                .filter(l->l.getLogicModule() instanceof ActionLogicModule)
                .filter(l->l!=LogicModuleRegistry.UnidentifiedFailure)
                .forEach(l->required.add(l.getLogicModule()));
        //required.stream().map(f->f.getClass().getSimpleName()).forEach(System.out::println);
    }

    public void setKnownFailureClasses(Set<LogicModule> knownFailureClasses) {
        this.knownFailureClasses = knownFailureClasses;
    }

}
