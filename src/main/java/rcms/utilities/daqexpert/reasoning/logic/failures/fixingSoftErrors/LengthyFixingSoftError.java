package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

public class LengthyFixingSoftError extends KnownFailure implements Parameterizable {

	public LengthyFixingSoftError() {
		this.name = "Lengthy fixing-soft-error";
	}

	@Override
	public void parametrize(Properties properties) {

		try {
			this.thresholdPeriod = Integer.parseInt(
					properties.getProperty(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey()));

			this.description = "Level zero in FixingSoftError longer than " + (thresholdPeriod / 1000)
					+ " sec. This is caused by subsystem(s) {{SUBSYSTEM}}";

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}
	}

	private int thresholdPeriod;
	private long timestampOfBegin;
	private static final String levelZeroProblematicState = "FixingSoftError";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		boolean result = false;

		String currentState = daq.getLevelZeroState();

		if (levelZeroProblematicState.equalsIgnoreCase(currentState)) {
			if (timestampOfBegin == 0) {
				timestampOfBegin = daq.getLastUpdate();
			} else {

				if (timestampOfBegin + thresholdPeriod < daq.getLastUpdate()) {
					result = true;
					for (SubSystem subsystem : daq.getSubSystems()) {
						if (levelZeroProblematicState.equalsIgnoreCase(subsystem.getStatus())) {
							context.register("SUBSYSTEM", subsystem.getName());
						}
					}
				}

			}
		} else {
			timestampOfBegin = 0L;
		}

		return result;
	}

}
