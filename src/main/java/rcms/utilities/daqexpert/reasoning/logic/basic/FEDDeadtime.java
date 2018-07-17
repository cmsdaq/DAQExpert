package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.functions.FedPrinter;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;

/**
 * This logic module identifies individual FED deadtime
 */
public class FEDDeadtime extends ContextLogicModule implements Parameterizable {

	private float threshold;

	public FEDDeadtime() {
		this.name = "FED deadtime";
		this.priority = ConditionPriority.DEFAULTT;
		this.threshold = 0;
	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.ExpectedRate);

		declareAffected(LogicModuleRegistry.PartitionDeadtime);
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		boolean expectedRate = results.get(ExpectedRate.class.getSimpleName()).getResult();
		if (!expectedRate)
			return false;

		boolean result = false;

		Iterator<FED> i = daq.getFeds().iterator();

		while (i.hasNext()) {
			FED fed = i.next();
			if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
				float deadPercentage = 0;
				deadPercentage += fed.getPercentBusy();
				deadPercentage += fed.getPercentWarning();

				if (deadPercentage > threshold) {
					result = true;
					contextHandler.registerObject("PROBLEM-FED", fed, new FedPrinter());
					contextHandler.registerObject("PROBLEM-SUBSYSTEM", fed.getTtcp().getSubsystem(), s->s.getName());
					contextHandler.registerForStatistics("DEADTIME",deadPercentage,"%",1);
				}
			}
		}

		return result;
	}

	@Override
	public void parametrize(Properties properties) {
		this.threshold = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED, this.getClass());
		this.description = "Deadtime of fed(s) {{PROBLEM-FED}} in subsystem(s) {{PROBLEM-SUBSYSTEM}} is {{DEADTIME}} , the threshold is " + threshold + "%";
	}

}
