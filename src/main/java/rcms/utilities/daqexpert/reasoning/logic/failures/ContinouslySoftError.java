package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;

public class ContinouslySoftError extends KnownFailure implements Parameterizable {

	public ContinouslySoftError() {
		this.name = "Detected continous soft error";
		this.pastOccurrences = new ArrayList<>();
		this.previousResult = false;
		this.previousState = "";
	}

	private static final String levelZeroProblematicState = "FixingSoftError";

	private List<Date> pastOccurrences;
	private Date lastFinish;
	private boolean previousResult;
	private String previousState;
	private int thresholdPeriod;
	private int mergePeriod;
	private int occurrencesThreshold;

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {
		boolean currentResult = false;
		String currentState = daq.getLevelZeroState();

		// 1. clear too old occurrences
		Iterator<Date> i = this.pastOccurrences.iterator();
		Date repeatThreshold = new Date(daq.getLastUpdate() - thresholdPeriod);
		Date mergeThreshold = new Date(daq.getLastUpdate() - mergePeriod);
		while (i.hasNext()) {
			Date date = i.next();
			if (date.before(repeatThreshold)) {
				i.remove();
			}
		}

		if (levelZeroProblematicState.equalsIgnoreCase(currentState)) {

			if (!previousState.equals(currentState)) {
				pastOccurrences.add(new Date(daq.getLastUpdate()));
			}

			if (pastOccurrences.size() > occurrencesThreshold) {
				currentResult = true;
				lastFinish = new Date(daq.getLastUpdate());
			}

		}

		/* Connect multiple into one event */
		if (previousResult && !currentResult) {

			if (lastFinish != null && lastFinish.after(mergeThreshold)) {
				currentResult = true;
			}
		}

		this.previousState = currentState;
		this.previousResult = currentResult;
		return currentResult;
	}

	@Override
	public void parametrize(Properties properties) {

		try {
			this.thresholdPeriod = Integer
					.parseInt(properties.getProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_PERIOD.getKey()));

			this.mergePeriod = Integer
					.parseInt(properties.getProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_KEEP.getKey()));
			this.occurrencesThreshold = Integer
					.parseInt(properties.getProperty(Setting.EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_COUNT.getKey()));
			this.description = "Level zero in FixingSoftError more than 3 times in past "
					+ (thresholdPeriod / 1000 / 60) + " min";

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}
	}

}
