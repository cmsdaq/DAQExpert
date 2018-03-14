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
						TTSState pmTTSState = getParitionState(ttcp);
						if (pmTTSState == TTSState.DISCONNECTED) {

							contextHandler.register("PROBLEM-SUBSYSTEM", subSystem.getName());
							contextHandler.register("PROBLEM-PARTITION", ttcp.getName());
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
			
			logger.trace("Disconnected TTCP: " + disconnectedTtcp.getName());
			
			TTSState fmmState = TTSState.getByCode(disconnectedTtcp.getTtsState());
			if (fmmState == TTSState.DISCONNECTED) {
				FMM disconnectedFMM = disconnectedTtcp.getFmm();
				logger.trace("Disconnected fmm: " + disconnectedFMM.getUrl() + " geoslot " + disconnectedFMM.getGeoslot()+ " has feds: " + disconnectedFMM.getFeds().size());

				contextHandler.register("PROBLEM-FMM-GEOSLOT", disconnectedFMM.getGeoslot());
				contextHandler.register("PROBLEM-FMM-URL", disconnectedFMM.getUrl());
				contextHandler.register("PROBLEM-FMM-SERVICE", disconnectedFMM.getServiceName());
				Set<FED> disconnectedFEDs = new HashSet<>();
				for (FED fed : disconnectedTtcp.getFeds()) {
					logger.trace("Checkign FED " + fed.getSrcIdExpected() + ": " + fed.getTtsState());
					if (!isMasked(fed)) {
						TTSState fedState = TTSState.getByCode(fed.getTtsState());
						if (fedState == TTSState.DISCONNECTED) {
							logger.info("Found disconnected fed: " + fed.getSrcIdExpected());
							disconnectedFEDs.add(fed);
						}
					}
				}
				if (disconnectedFEDs.size() > 0) {
					for (FED disconnectedFED : disconnectedFEDs) {
						contextHandler.register("PROBLEM-FED", disconnectedFED.getSrcIdExpected());
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