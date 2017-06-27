package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.FEDHierarchyRetriever;
import rcms.utilities.daqexpert.reasoning.logic.failures.helper.LogicModuleHelper;

/**
 * Logic module identifying 6 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 * @deprecated
 */
public class FlowchartCase6 extends KnownFailure {

	public FlowchartCase6() {
		this.name = "Backpressure detected";

		this.description = "A FED stopped sending data in subsystem {{FROZENSUBSYSTEM}}. Therefore, FED {{FED}} is backpressured, which causes partition {{TTCP}} of subsystem {{SUBSYSTEM}} to be in {{TTCPSTATE}} TTS state. There is NOTHING wrong with {{SUBSYSTEM}}.";

		this.action = new SimpleAction("Try to recover: Stop the run",
				"Red & green recycle the subsystem {{FROZENSUBSYSTEM}} (whose FED stopped sending data)",
				"Start new Run (Try 1 time)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{FROZENSUBSYSTEM}} (whose FED stopped sending data) to inform",
				"Problem not fixed: Call the DOC for the subsystem {{FROZENSUBSYSTEM}} (whose FED stopped sending data)");

	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Boolean> results) {

		if (!results.get(NoRateWhenExpected.class.getSimpleName()))
			return false;

		assignPriority(results);

		boolean result = false;

		String daqstate = daq.getDaqState();

		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			// FEDs which are backpressured by the DAQ, we do NOT report those
			Set<FED> fedsBackpressuredByDaq = new HashSet<>();

			Set<SubSystem> victimSubsystems = new HashSet<>();

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {
					if (!ttcp.isMasked()) {

						TTSState currentState = getParitionState(ttcp);
						if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

							Map<FED, Set<FED>> feds = FEDHierarchyRetriever.getFEDHierarchy(ttcp);

							for (Entry<FED, Set<FED>> fed : feds.entrySet()) {

								TTSState currentFedState = TTSState.getByCode(fed.getKey().getTtsState());
								if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)) {

									if (fed.getValue().size() > 0) {

										for (FED dep : fed.getValue()) {

											if (dep.getPercentBackpressure() > 0F) {

												context.register("FED", dep.getSrcIdExpected());
												context.register("FEDSTATE", "(" + currentFedState.name()
														+ " seen on FED" + fed.getKey().getSrcIdExpected() + ")");
												fedsBackpressuredByDaq.add(fed.getKey());
												victimSubsystems.add(subSystem);
												result = true;
											}

										}

									} else {

										if (fed.getKey().getPercentBackpressure() > 0F) {
											context.register("FED", fed.getKey().getSrcIdExpected());
											context.register("FEDSTATE", currentFedState.name());
											fedsBackpressuredByDaq.add(fed.getKey());
											result = true;
										}

									}

								}
							}

							if (result) {
								context.register("TTCP", ttcp.getName());
								context.register("TTCPSTATE", currentState.name());
								context.register("SUBSYSTEM", subSystem.getName());
							}
						}
					}
				}
			}

			if (result) {

				// get the feds whose event counter is behind the one of the
				// TCDS fed
				List<FED> behindFeds = LogicModuleHelper.getFedsWithFewerFragments(daq);

				for (FED fed : behindFeds) {
					if (!fedsBackpressuredByDaq.contains(fed)) {

						/*
						 * make sure to not include victim subsystem (subsystem
						 * when you observe the problem results) in the report
						 * as problematic ones
						 */
						if (!victimSubsystems.contains(fed.getTtcp().getSubsystem())) {
							/*
							 * this FED stopped sending data for no apparent
							 * reason (note that context.register() ignores
							 * duplicate entries)
							 */
							context.register("FROZENSUBSYSTEM", fed.getTtcp().getSubsystem().getName());
						}
					}
				} // loop over FEDs
			} // if condition is satisfied

		}

		return result;
	}

}
