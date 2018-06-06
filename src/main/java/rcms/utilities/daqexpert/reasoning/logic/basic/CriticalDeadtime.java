package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies deadtime
 */
public class CriticalDeadtime extends ContextLogicModule implements Parameterizable {

    private float threshold;

    private static final Logger logger = Logger.getLogger(CriticalDeadtime.class);

	public CriticalDeadtime() {
        this.name = "Deadtime";
		this.priority = ConditionPriority.IMPORTANT;
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.ExpectedRate);
		require(LogicModuleRegistry.BeamActive);


		declareAffected(LogicModuleRegistry.Deadtime);
	}

	/**
	 * Dead time during running
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean expectedRate = results.get(ExpectedRate.class.getSimpleName()).getResult();

        if (!expectedRate) {
            return false;
        }

        double deadtime = getDeadtime(daq, results);

        if (deadtime > threshold) {
            contextHandler.registerForStatistics("DEADTIME", deadtime, "%", 1);
			return true;
        } else
		return false;
	}

	private double getDeadtime(DAQ daq, Map<String, Output> results){
		try {
			if (results.get(BeamActive.class.getSimpleName()).getResult()) {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant()
						.get("beamactive_total");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant().get("total");
			}
		} catch (NullPointerException e) {
			logger.warn("Instantaneous deadtime value is not available. Using per lumi section.");
			if (results.get(BeamActive.class.getSimpleName()).getResult()) {
				return daq.getTcdsGlobalInfo().getDeadTimes()
						.get("beamactive_total");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimes().get("total");
			}
		}
	}

    @Override
    public void parametrize(Properties properties) {
        try {
            this.threshold = Integer
                    .parseInt(properties.getProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL.getKey()));

            this.description = "Deadtime during running is {{DEADTIME}}, the threshold is " + threshold + "%";
        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
        }

    }

}
