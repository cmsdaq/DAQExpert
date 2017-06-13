/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.List;
import java.util.Map;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.Counter;

/**
 *  Module firing when one or more RUs are in Failed state (and DAQ and L0
 *  are in Error). This is used to catch any otherwise unidentified
 *  error when RUs go into Failed state
 */
public class RuFailed extends KnownFailure {

	public RuFailed() {
		this.name = "RUs failed";

		this.description = "{{NUMFAILEDRUS}} RUs ({{RU}}) are in failed state for an unidentified reason. "
						+ "The most often occurring (({{MOSTFREQUENTERRORCOUNT}} times) error message is: {{MOSTFREQUENTERROR}}"
				;

		this.action = new SimpleAction(
				"Try to recover: Stop the run. Red & green recycle the DAQ. Start a new Run. (Try up to 2 times)",
				"Make an e-log entry.");
	}

	private final String ERROR_STATE = "ERROR";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		boolean result = false;

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			if (ERROR_STATE.equalsIgnoreCase(l0state) && ERROR_STATE.equalsIgnoreCase(daqstate)) {

				List<RU> failedRus = daq.getRusInState("Failed");

				if (! failedRus.isEmpty()) {

					Counter<String> stateCounter = new Counter<>();

					for (RU ru : failedRus) {

						context.register("RU", ru.getHostname());
						stateCounter.add(ru.getErrorMsg());

					}

					context.register("NUMFAILEDRUS", failedRus.size());

					Map.Entry<String, Integer> mostFreqError = stateCounter.getMaximumEntry();
					context.register("MOSTFREQUENTERROR", mostFreqError.getKey());
					context.register("MOSTFREQUENTERRORCOUNT", mostFreqError.getValue());

					result = true;

				} // if at least one failed RU

			} // if both L0 and DAQ in ERROR

		} // if not RUNBLOCKED

		return result;
	}
}
