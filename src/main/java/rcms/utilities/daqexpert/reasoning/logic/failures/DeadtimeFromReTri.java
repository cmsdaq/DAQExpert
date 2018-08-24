package rcms.utilities.daqexpert.reasoning.logic.failures;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.BeamActive;
import rcms.utilities.daqexpert.reasoning.logic.basic.CriticalDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

import java.util.Map;
import java.util.Properties;

/**
 * Module firing when there is deadtime from ReTri
 */
public class DeadtimeFromReTri extends KnownFailure implements Parameterizable {

    private double contributionThresholdInPercent;

    public DeadtimeFromReTri() {
        this.name = "High ReTri deadtime";
        this.action = new SimpleAction(
                "Adding random triggers may reduce the deadtime from ReTri. Discuss this with the shift leader.");

    }

    @Override
    public void declareRelations(){
        require(LogicModuleRegistry.CriticalDeadtime);
        require(LogicModuleRegistry.BeamActive);
        declareAffected(LogicModuleRegistry.Deadtime);
    }

    @Override
    public boolean satisfied(DAQ daq) {

        boolean result = false;
        if (!getOutputOf(LogicModuleRegistry.CriticalDeadtime).getResult())
            return false;

        //assignPriority(results);

        boolean beamActive = getOutputOf(LogicModuleRegistry.BeamActive).getResult();
        double retriContributionToDeadtime = getReTriDeadtime(daq, beamActive);

        if (retriContributionToDeadtime > contributionThresholdInPercent) {
            contextHandler.registerForStatistics("RETRI_CONTRIBUTION", retriContributionToDeadtime, "%", 1);
            result = true;
        }

        return result;
    }

    private double getReTriDeadtime(DAQ daq, boolean beamActive) {
        try {
            if (beamActive) {
                return daq.getTcdsGlobalInfo().getDeadTimesInstant()
                        .get("beamactive_retri");

            } else {
                return daq.getTcdsGlobalInfo().getDeadTimesInstant().get("retri");
            }
        } catch (NullPointerException e) {
            if (beamActive) {
                return daq.getTcdsGlobalInfo().getDeadTimes()
                        .get("beamactive_retri");

            } else {
                return daq.getTcdsGlobalInfo().getDeadTimes().get("retri");
            }
        }
    }

    @Override
    public void parametrize(Properties properties) {
        this.contributionThresholdInPercent = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_RETRI, this.getClass());
        this.description = "A large contribution ({{RETRI_CONTRIBUTION}}, the threshold is " + contributionThresholdInPercent + "%) of the deadtime comes from the resonant trigger protection (ReTri). This can happen in fills with only a few bunches.";
        this.briefDescription = "A large contribution ({{RETRI_CONTRIBUTION}} of the deadtime comes from the resonant trigger protection (ReTri).";
    }
}
