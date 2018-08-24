package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
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
		this.holdNotifications = true;
		this.threshold = 0;
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.BeamActive);
	}

	/**
	 * Dead time when greater than a threshold%
	 */
	@Override
	public boolean satisfied(DAQ daq) {

		boolean beamActive = getOutputOf(LogicModuleRegistry.BeamActive).getResult();
		double deadtime = getDeadtime(daq, beamActive);
		if (deadtime > threshold){
			contextHandler.registerForStatistics("DEADTIME", deadtime,"%",1);
			return true;
		}
		else
			return false;
	}


	private double getDeadtime(DAQ daq, boolean beamActive){

		try {
			if (beamActive) {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant()
						.get("beamactive_total");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant().get("total");
			}
		} catch (NullPointerException e) {
			logger.debug("Instantaneous deadtime value is not available. Using per lumi section.");
			if (beamActive) {
				return daq.getTcdsGlobalInfo().getDeadTimes()
						.get("beamactive_total");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimes().get("total");
			}
		}
	}

	@Override
	public void parametrize(Properties properties) {
		this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL, this.getClass());
		this.description = "Deadtime is {{DEADTIME}}, the threshold is " + threshold + "%";

	}

}
