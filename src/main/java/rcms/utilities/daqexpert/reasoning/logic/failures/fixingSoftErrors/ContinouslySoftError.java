package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

public class ContinouslySoftError extends KnownFailure implements Parameterizable {

	public ContinouslySoftError() {
		this.name = "Continuous fixing-soft-error";

		/* default action */
		ConditionalAction action = new ConditionalAction("Call DOC of subsystem {{SUBSYSTEM}}");

		/* ES specific instructions */
		action.addContextSteps("ES", "Stop the run and re-start it",
				"If 1) doesn't work and DAQ is in the same condition as before, stop the run and red-recycle ES");

		/* Pixel specific instructions */
		action.addContextSteps("Pixel", "Check DCS",
				"If problem in DCS (sectors turned off) ask DCS shifter to call Pixel DOC",
				"If no problem in DCS call Pixel DOC immediately");

		/* CTPPS specific instructions */
		action.addContextSteps("CTPPS", "<<RedRecycle::{{SUBSYSTEM}}>>",
				"Call DOC of subsystem {{SUBSYSTEM}}");

		this.action = action;

		this.pastOccurrences = new ArrayList<>();
		this.previousResult = false;
		this.previousState = "";
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
					+ (thresholdPeriod / 1000 / 60) + " min. This is caused by subsystem(s) {{SUBSYSTEM_WITH_COUNTS}}";

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}
	}

	private static final Logger logger = Logger.getLogger(ContinouslySoftError.class);

	private static final String levelZeroProblematicState = "FixingSoftError";

	private List<Pair<Date, List<String>>> pastOccurrences;
	private Date lastFinish;
	private boolean previousResult;
	private String previousState;
	private int thresholdPeriod;
	private int mergePeriod;
	private int occurrencesThreshold;

	private List<String> problemStates = Arrays.asList("FixingSoftError", "RunningSoftErrorDetected");

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {
		boolean currentResult = false;
		String currentState = daq.getLevelZeroState();

		// 1. clear too old occurrences
		Iterator<Pair<Date, List<String>>> i = this.pastOccurrences.iterator();
		Date repeatThreshold = new Date(daq.getLastUpdate() - thresholdPeriod);
		Date mergeThreshold = new Date(daq.getLastUpdate() - mergePeriod);
		while (i.hasNext()) {
			Pair<Date, List<String>> entry = i.next();
			if (entry.getLeft().before(repeatThreshold)) {
				i.remove();
			}
		}

		if (levelZeroProblematicState.equalsIgnoreCase(currentState)) {

			if (!previousState.equals(currentState)) {

				List<String> subsystemsInFixing = new ArrayList<>();
				for (SubSystem subsystem : daq.getSubSystems()) {
					if (problemStates.contains(subsystem.getStatus())) {
						logger.debug("Subsystem " + subsystem.getName() + " if in one of problematic states: "
								+ subsystem.getStatus());
						subsystemsInFixing.add(subsystem.getName());
					}
				}

				pastOccurrences.add(Pair.of(new Date(daq.getLastUpdate()), subsystemsInFixing));
			}

			Map<String, Integer> coutsPerSubsystem = new HashMap<>();

			for (Pair<Date, List<String>> occurrence : pastOccurrences) {
				for (String subsystem : occurrence.getRight()) {
					if (coutsPerSubsystem.containsKey(subsystem)) {
						int count = coutsPerSubsystem.get(subsystem);
						coutsPerSubsystem.put(subsystem, count + 1);
					} else {
						coutsPerSubsystem.put(subsystem, 1);
					}
				}
			}

			boolean existsSubsystemWithTooManyResets = false;
			for (int count : coutsPerSubsystem.values()) {
				if (count > occurrencesThreshold) {
					existsSubsystemWithTooManyResets = true;
				}
			}

			if (existsSubsystemWithTooManyResets) {
				currentResult = true;
				lastFinish = new Date(daq.getLastUpdate());

				for (Entry<String, Integer> count : coutsPerSubsystem.entrySet()) {
					if (count.getValue() > occurrencesThreshold) {

						String problematicSubsystem = count.getKey();

						logger.debug("Registering " + count.getValue());

						contextHandler.register("SUBSYSTEM", problematicSubsystem);

						// this is used for displaying information about the number
						// of fixing soft error cycles
						contextHandler.register("SUBSYSTEM_WITH_COUNTS",
										problematicSubsystem + " " + count.getValue() + " time(s)");

						contextHandler.setActionKey(problematicSubsystem);
					}
				}
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



}
