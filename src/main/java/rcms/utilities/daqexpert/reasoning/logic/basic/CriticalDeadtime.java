package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.junit.internal.runners.statements.Fail;
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
public class CriticalDeadtime extends ContextLogicModule implements Parameterizable {

    private float threshold;

    private static final Logger logger = Logger.getLogger(CriticalDeadtime.class);

	public CriticalDeadtime() {
        this.name = "Deadtime";
		this.priority = ConditionPriority.IMPORTANT;
	}

	/** Do not display this keys as contribution, these are resulting deadtimes */
	private List<String> contributionFilter = Arrays.asList("total", "beamactive_total");

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

        boolean beamActive = results.get(BeamActive.class.getSimpleName()).getResult();

        if (!expectedRate) {
            return false;
        }

        double deadtime = getTotalDeadtime(daq, beamActive);
		Map<String, Double> contributions = getContributions(daq, beamActive);

		contributions.entrySet().stream()
				.filter(c->!(contributionFilter.contains(c.getKey())))
				.forEach(c -> contextHandler.registerForStatistics("CONTRIBUTIONS_" + c.getKey(), c.getValue(), "%",1));

        if (deadtime > threshold) {
            contextHandler.registerForStatistics("DEADTIME", deadtime, "%", 1);
			return true;
        } else
		return false;
	}

	/**
	 * Get deadtime. Returns instant deadtimes if available. Per lumisection otherwise
	 */
	private Map<String, Double> getDeadtimes(DAQ daq){
		if(daq.getTcdsGlobalInfo().getDeadTimesInstant() != null && !daq.getTcdsGlobalInfo().getDeadTimesInstant().isEmpty()) {
			return daq.getTcdsGlobalInfo().getDeadTimesInstant();
		} else {
			return daq.getTcdsGlobalInfo().getDeadTimes();
		}
	}

	/**
	 * Returns total deadtime depending on beamactive flag.
	 */
	private double getTotalDeadtime(DAQ daq, boolean beamactive){
		if (beamactive) {
			return getDeadtimes(daq).get("beamactive_total");
		} else {
			return getDeadtimes(daq).get("total");
		}
	}

	/**
	 * Get contributions to the deadtime depending to beamactive flag.
	 */
	private Map<String, Double> getContributions(DAQ daq, boolean beamActive){
		Stream<Map.Entry<String,Double>> stream;
		if(beamActive){
			stream = getDeadtimes(daq).entrySet().stream().filter(e->e.getKey().startsWith("beamactive"));
		} else{
			stream = getDeadtimes(daq).entrySet().stream().filter(e->!e.getKey().startsWith("beamactive"));
		}
		return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	}

    @Override
    public void parametrize(Properties properties) {
		this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL, this.getClass());
		this.description = "Deadtime during running is {{DEADTIME}}, the threshold is " + threshold + "%. There are following contributions: " +
				"{{CONTRIBUTIONS_*}}";
    }

}
