package rcms.utilities.daqexpert.reasoning.logic.basic;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.Map;
import java.util.Properties;

/**
 * This logic module identifies TTS deadtime
 */
public class TTSDeadtime extends KnownFailure implements Parameterizable {

    private float threshold;

    private static final Logger logger = Logger.getLogger(TTSDeadtime.class);

	public TTSDeadtime() {
        this.name = "TTS Deadtime";
		this.priority = ConditionPriority.IMPORTANT;
		this.action = new SimpleAction("Look at CPM page");

	}

	/**
	 * Dead time during running
	 */
	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

        boolean transition;
        boolean expectedRate;
		expectedRate = results.get(ExpectedRate.class.getSimpleName());
        transition = results.get(Transition.class.getSimpleName());

        if (!(expectedRate && !transition)) {
            return false;
        }

        double deadtime = getDeadtime(daq, results);

        if (deadtime > threshold) {
            context.registerForStatistics("DEADTIME", deadtime, "%", 1);
			return true;
        } else
		return false;
	}

	private double getDeadtime(DAQ daq, Map<String, Boolean> results){
		try {
			if (results.get(BeamActive.class.getSimpleName())) {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant()
						.get("beamactive_tts");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimesInstant().get("tts");
			}
		} catch (NullPointerException e) {
			logger.warn("Instantaneous TTS deadtime value is not available. Using per lumi section.");
			if (results.get(BeamActive.class.getSimpleName())) {
				return daq.getTcdsGlobalInfo().getDeadTimes()
						.get("beamactive_tts");

			} else {
				return daq.getTcdsGlobalInfo().getDeadTimes().get("tts");
			}
		}
	}

    @Override
    public void parametrize(Properties properties) {
		this.threshold = FailFastParameterReader.getIntegerParameter(properties,Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TTS,this.getClass());
		this.description = "TTS Deadtime during running is {{DEADTIME}}, the threshold is " + threshold + "%";
    }

}
