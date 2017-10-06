package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies deadtime
 */
public class Deadtime extends ContextLogicModule implements Parameterizable {

	private float threshold;

	private final static Logger logger = Logger.getLogger(Deadtime.class);

	public Deadtime() {
		this.name = "Raw deadtime";
		this.priority = ConditionPriority.DEFAULTT;
		this.threshold = 0;
	}

	/**
	 * Dead time when greater than a threshold%
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		double deadtime = getDeadtime(daq, results);
		if (deadtime > threshold){
			context.registerForStatistics("DEADTIME", deadtime,"%",1);
			return true;
		}
		else
			return false;
	}


	private double getDeadtime(DAQ daq, Map<String, Boolean> results){
		try {
			if (results.get(BeamActive.class.getSimpleName())) {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant()
						.get("beamactive_total");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant().get("total");
			}
		} catch (NullPointerException e) {
			logger.warn("Instantaneous deadtime value is not available. Using per lumi section.");
			if (results.get(BeamActive.class.getSimpleName())) {
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

			this.description = "Deadtime is {{DEADTIME}}, the threshold is " + threshold + "%";
		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}

	}

}
