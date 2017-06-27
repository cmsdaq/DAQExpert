package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
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

		this.description = "TTCP {{TTCP}} of subsystem {{SUBSYSTEM}} in {{TTCPSTATE}} TTS state, and FED {{FED}} is backpressured. "
				+ "Backpressure is going through that FED, it's in {{FEDSTATE}} but there is NOTHING wrong with it. "
				+ "A FED stopped sending data in subsystem {{FROZENSUBSYSTEM}}.";

		this.action = new SimpleAction("Try to recover: Stop the run",
				"Red & green recycle the subsystem {{FROZENSUBSYSTEM}} (whose FED stopped sending data)",
				"Start new Run (Try 1 time)",
				"Problem fixed: Make an e-log entry. Call the DOC of the subsystem {{FROZENSUBSYSTEM}} (whose FED stopped sending data) to inform",
				"Problem not fixed: Call the DOC for the subsystem {{FROZENSUBSYSTEM}} (whose FED stopped sending data)");

	}

	private static final Logger logger = Logger.getLogger(FlowchartCase6.class);

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

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {
					if (!ttcp.isMasked()) {

						TTSState currentState = getParitionState(ttcp);
						if (currentState == TTSState.BUSY || currentState == TTSState.WARNING) {

							for (FED fed : ttcp.getFeds()) {

								if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
									TTSState currentFedState = TTSState.getByCode(fed.getTtsState());
									if ((currentFedState == TTSState.BUSY || currentFedState == TTSState.WARNING)
											&& fed.getPercentBackpressure() > 0F) {

										context.register("TTCP", ttcp.getName());
										context.register("TTCPSTATE", currentState.name());
										context.register("SUBSYSTEM", subSystem.getName());
										context.register("FED", fed.getSrcIdExpected());
										context.register("FEDSTATE", currentFedState.name());

										fedsBackpressuredByDaq.add(fed);

										logger.debug("M6: " + name + " with fed " + fed.getId() + " in backpressure at "
												+ new Date(daq.getLastUpdate()));
										result = true;
									}
								}
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

						// this FED stopped sending data for no apparent reason
						// (note that context.register() ignores duplicate
						// entries)
						context.register("FROZENSUBSYSTEM", fed.getTtcp().getSubsystem().getName());
					}
				} // loop over FEDs
			} // if condition is satisfied

		}

		return result;
	}

}
