package rcms.utilities.daqexpert.reasoning.logic.failures.disconnected;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

/**
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class DisconnectedAnalyzer extends KnownFailure {

	private static final Logger logger = Logger.getLogger(DisconnectedAnalyzer.class);

	public DisconnectedAnalyzer() {
	}

	public DisconnectedSubcase detectDisconnect(DAQ daq) {

		String daqstate = daq.getDaqState();
		if (!"RUNBLOCKED".equalsIgnoreCase(daqstate)) {

			for (SubSystem subSystem : daq.getSubSystems()) {

				for (TTCPartition ttcp : subSystem.getTtcPartitions()) {
					if (!ttcp.isMasked()) {
						TTSState pmTTSState = TTSState.getByCode(ttcp.getTcds_pm_ttsState());
						if (pmTTSState == TTSState.DISCONNECTED) {

							context.register("PROBLEM-SUBSYSTEM", subSystem.getName());
							context.register("PROBLEM-PARTITION", ttcp.getName());
							return detectSubcase(daq, ttcp);

						}
					}
				}
			}

		}

		return DisconnectedSubcase.Unknown;
	}

	protected DisconnectedSubcase detectSubcase(DAQ daq, TTCPartition disconnectedTtcp) {

		// LEGACY partition (99% it's true)
		if (disconnectedTtcp.getFmm() != null) {
			TTSState fmmState = TTSState.getByCode(disconnectedTtcp.getTtsState());
			if (fmmState == TTSState.DISCONNECTED) {
				FMM disconnectedFMM = disconnectedTtcp.getFmm();

				context.register("PROBLEM-FMM-GEOSLOT", disconnectedFMM.getGeoslot());
				context.register("PROBLEM-FMM-URL", disconnectedFMM.getUrl());
				context.register("PROBLEM-FMM-SERVICE", disconnectedFMM.getServiceName());
				Set<FED> disconnectedFEDs = new HashSet<>();
				for (FED fed : disconnectedFMM.getFeds()) {
					if (isMasked(fed)) {
						TTSState fedState = TTSState.getByCode(fed.getTtsState());
						if (fedState == TTSState.DISCONNECTED) {
							disconnectedFEDs.add(fed);
						}
					}
				}
				if (disconnectedFEDs.size() > 0) {
					for (FED disconnectedFED : disconnectedFEDs) {
						context.register("PROBLEM-FED", disconnectedFED.getSrcIdExpected());
					}
					return DisconnectedSubcase.FEDDisconnected;
				} else {
					return DisconnectedSubcase.FMMProblem;
				}

			} else {
				return DisconnectedSubcase.ProblemWithPi;
			}
		}
		// mTCU partition
		else {
			return DisconnectedSubcase.PiDisconnected;
		}

	}
}

enum DisconnectedSubcase {
	PiDisconnected, ProblemWithPi, FEDDisconnected, FMMProblem, Unknown;

}