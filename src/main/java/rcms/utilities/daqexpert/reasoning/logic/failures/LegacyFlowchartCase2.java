package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.ConditionalAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.CorruptedData;

/**
 * Logic module identifying flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class LegacyFlowchartCase2 extends KnownFailure {

	public LegacyFlowchartCase2() {
		this.name = "Corrupted data received";
		this.description = "DAQ and level 0 in error state. "
				+ "A RU {{PROBLEM-RU}} is in Failed state. A FED {{PROBLEM-FED}} has sent corrupted data to the DAQ. "
				+ "Problem FED belongs to subsystem {{PROBLEM-SUBSYSTEM}}";

		this.briefDescription = "A FED {{PROBLEM-SUBSYSTEM}}/{{PROBLEM-FED}} has sent corrupted data";

		/* default action */
		ConditionalAction action = new ConditionalAction(
				"Try to recover: Stop the run. Red & green recycle both the DAQ and the subsystem {{PROBLEM-SUBSYSTEM}}. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data)");

		/* ecal specific case */
		action.addContextSteps("ECAL", "Try to stop/start the run (Red recycle DAQ only)",
				"If this doesn't help: Stop the run. Red & green recycle both the DAQ and the subsystem {{PROBLEM-SUBSYSTEM}}. Start new Run. (Try up to 2 times)",
				"Problem fixed: Make an e-log entry. Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data) to inform about the problem",
				"Problem not fixed: Call the DOC of {{PROBLEM-SUBSYSTEM}} (subsystem that sent corrupted data)");


		this.action = action;

	}

	@Override
	public void declareRelations(){
		require(LogicModuleRegistry.NoRateWhenExpected);
		require(LogicModuleRegistry.CorruptedData);
		declareAffected(LogicModuleRegistry.FlowchartCase5);
	}

	private static Logger logger = Logger.getLogger(LegacyFlowchartCase2.class);
	private final String ERROR_STATE = "ERROR";

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()).getResult())
			return false;
		
		if(results.get(CorruptedData.class.getSimpleName()).getResult())
			return false;

		assignPriority(results);

		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();
		boolean result = false;
		int i = 0;

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			if (ERROR_STATE.equalsIgnoreCase(l0state) && ERROR_STATE.equalsIgnoreCase(daqstate)) {

				List<RU> failedRus = daq.getRusInState("Failed");

				if (failedRus.isEmpty()) {
					return false;
				}

				for (RU ru : failedRus) {

					i++;
					contextHandler.register("PROBLEM-RU", ru.getHostname());

				}

				for (FED fed : daq.getFeds()) {

					if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
						if (fed.getRuFedDataCorruption() > 0) {

							TTCPartition ttcp = fed.getTtcp();
							String ttcpName = "-";
							String subsystemName = "-";

							if (ttcp != null) {
								ttcpName = ttcp.getName();
								if (ttcp.getSubsystem() != null)
									subsystemName = ttcp.getSubsystem().getName();
							}
							contextHandler.register("PROBLEM-FED", fed.getSrcIdExpected());
							contextHandler.register("PROBLEM-PARTITION", ttcpName);
							contextHandler.register("PROBLEM-SUBSYSTEM", subsystemName);
							contextHandler.setActionKey(subsystemName);
							i++;

							result = true;
						}
					}
				}

				logger.debug("FC2 " + i);
			}
		}
		return result;
	}
}