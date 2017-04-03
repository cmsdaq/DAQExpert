package rcms.utilities.daqexpert.reasoning.logic.failures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.reasoning.base.enums.TTSState;

/**
 * Logic module identifying 6 flowchart case.
 * 
 * @see flowchart at https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class BackpressureAnalyzer extends KnownFailure {

	private static final Logger logger = Logger.getLogger(BackpressureAnalyzer.class);

	/**
	 * Rregex for getting ttc partition and FED source id which caused the sync
	 * loss from the RU exception message
	 */
	private final Pattern syncLossPattern = Pattern.compile(
			"Caught exception: exception::MismatchDetected 'Mismatch detected: expected evb id .*, but found evb id .* in data block from FED (\\d+) \\((.+)\\)' raised at");

	public BackpressureAnalyzer() {
	}

	private static Pair<Long, Subcase> result;

	protected Subcase detectBackpressure(DAQ daq) {

		logger.trace("++++++++++++++++++++++");
		logger.trace("Detecting backpressure");

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {
				if (!ttcp.isMasked()) {

					TTSState ttsState = TTSState.getByCode(ttcp.getTtsState());
					TTSState ttsStateAtAPV = TTSState.getByCode(ttcp.getTcds_apv_pm_ttsState());
					TTSState ttsStateAtPM = TTSState.getByCode(ttcp.getTcds_pm_ttsState());

					if ((ttsState == TTSState.BUSY || ttsState == TTSState.WARNING)
							|| (ttsStateAtAPV == TTSState.BUSY || ttsStateAtAPV == TTSState.WARNING)
							|| (ttsStateAtPM == TTSState.BUSY || ttsStateAtPM == TTSState.WARNING)) {

						logger.trace("Found partition in B/W: TTCP " + ttcp.getName() + " of " + subSystem.getName());
						for (FED fed : ttcp.getFeds()) {

							if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
								// TODO: removed check B or W due to some has
								// null
								TTSState currentFedState = TTSState.getByCode(fed.getTtsState());

								if (fed.getPercentBackpressure() > 0F) {

									logger.trace("Found backpressured FED " + fed.getSrcIdExpected() + ", value: "
											+ fed.getPercentBackpressure());

									saveAffectedElementsContext(subSystem, ttcp, fed);

									// MODIFICATION for OOS: helps with OOS
									// OutOfSequenceTest:trgFedCase
									try {

										RU relatedRu = fed.getFrl().getSubFedbuilder().getFedBuilder().getRu();
										logger.trace("  Checking state of " + relatedRu.getHostname()
												+ " for OOS or corrupted from " + fed.getSrcIdExpected()
												+ " because of partition " + ttcp.getName());
										Subcase problemWithRu = checkProblemWithDataReceivedByRu(relatedRu);
										if (problemWithRu != null) {
											return problemWithRu;
										}
									} catch (NullPointerException e) {
									}

									Subcase subcase = detectSubcase(daq, fed);

									if (subcase != null) {
										switch (subcase) {

										case UnknownFilterfarmProblem:
											// this.description += "Caused by
											// unknown problem with
											// filterfarm.";
											break;

										case SpecificFedBlocking:
											// this.description += "Caused by
											// {{FED}} that stopped sending data
											// - it's the only FED in RU
											// {{RU}}";
											break;
										case Unknown:
											// this.description += "Could not
											// determine the source problem";
											break;

										}
									}
									return subcase;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private void saveAffectedElementsContext(SubSystem subSystem, TTCPartition ttcp, FED fed) {
		TTSState ttsState = TTSState.getByCode(ttcp.getTtsState());
		TTSState ttsStateAtAPV = TTSState.getByCode(ttcp.getTcds_apv_pm_ttsState());
		TTSState ttsStateAtPM = TTSState.getByCode(ttcp.getTcds_pm_ttsState());
		TTSState currentFedState = TTSState.getByCode(fed.getTtsState());

		String combinedTtsState = "";
		combinedTtsState += ttsState != null ? ttsState + "@FMM" : "";
		combinedTtsState += ttsStateAtAPV != null ? ttsStateAtAPV + "@APV" : "";
		combinedTtsState += ttsStateAtPM != null ? ttsStateAtPM + "@PM" : "";

		context.register("AFFECTED-TTCP", ttcp.getName());
		context.register("AFFECTED-SUBSYSTEM", subSystem.getName());
		context.register("AFFECTED-FED", fed.getSrcIdExpected());

		context.register("AFFECTED-TTCP-STATE", combinedTtsState);
		context.register("AFFECTED-FED-STATE", currentFedState.name());
	}

	private Subcase detectSubcase(DAQ daq, FED affectedFed) {

		RU relatedRu = null;
		String ruName = null;
		try {
			relatedRu = affectedFed.getFrl().getSubFedbuilder().getFedBuilder().getRu();
			ruName = relatedRu.getHostname();
		} catch (NullPointerException e) {
			ruName = "(RU not found)";
		}
		context.register("AFFECTED-RU", ruName);

		Subcase problemWithRu = checkProblemWithDataReceivedByRu(relatedRu);
		if (problemWithRu != null) {
			return problemWithRu;
		}

		logger.debug("#1 check: RU waiting for backpressured fed (" + affectedFed.getSrcIdExpected() + ")?");
		if (affectedFed.isRuFedWithoutFragments()) {
			logger.debug("#FOUND: link problem");
			return Subcase.LinkProblem;
		} else {

			logger.debug("#2 check: RU waiting for other FEDs in same FB?");
			boolean waitingForOtherFedsInFB = false;
			List<FED> notMaskedFedsOfRelatedRU = notMaskedFedsFromRU(relatedRu);
			Long maxTrigger = null, minTrigger = null;
			SubFEDBuilder minSFB = null, maxSFB = null;
			for (FED fed : notMaskedFedsOfRelatedRU) {
				if (fed.isRuFedWithoutFragments()) {
					waitingForOtherFedsInFB = true;
					context.register("PROBLEM-FED", fed.getSrcIdExpected());
					context.register("PROBLEM-SUBSYSTEM", fed.getTtcp().getSubsystem().getName());
					context.register("PROBLEM-TTCP", fed.getTtcp().getName());
				}
			}

			for (SubFEDBuilder subFedBuilder : relatedRu.getFedBuilder().getSubFedbuilders()) {
				if (minTrigger == null || minTrigger > subFedBuilder.getMinTrig()) {
					minTrigger = subFedBuilder.getMinTrig();
					minSFB = subFedBuilder;
				}
				if (maxTrigger == null || maxTrigger < subFedBuilder.getMaxTrig()) {
					maxTrigger = subFedBuilder.getMaxTrig();
					maxSFB = subFedBuilder;
				}
			}

			if (waitingForOtherFedsInFB) {
				logger.debug("#FOUND: ru is waiting for other feds in same fb");
				context.register("PROBLEM-FED-BUILDER",
						relatedRu.getFedBuilder() != null ? relatedRu.getFedBuilder().getName() : "not found");
				context.register("MIN-FRAGMENT-COUNT", minTrigger);
				context.register("MIN-FRAGMENT-PARTITION",
						minSFB != null ? minSFB.getTtcPartition().getName() : "not found");
				context.register("MAX-FRAGMENT-COUNT", maxTrigger);
				context.register("MAX-FRAGMENT-PARTITION",
						maxSFB != null ? maxSFB.getTtcPartition().getName() : "not found");
				return Subcase.WaitingForOtherFedsInFB;
			}

			logger.debug("#3 check: RU has > 0 requests?");
			if (relatedRu.getRequests() > 0) {
				logger.debug("#FOUND: ru " + relatedRu.getHostname() + " is stuck");
				return Subcase.RuIsStuck;
			} else {
				logger.trace("RU has 0 requests, check other RUs");
				// ru has 0 requests
				// check if other RU/EVM has requests
				Set<RU> rusWithManyRequests = new HashSet<>();
				for (RU ru : daq.getRus()) {
					if (ru.equals(relatedRu)) {
						// don't check this ru
					} else {
						if (ru.getRequests() > 0) {
							rusWithManyRequests.add(ru);
						}
					}
				}

				logger.debug("#4 check:  Other RUs has many requests?");
				if (rusWithManyRequests.size() == 0) {
					logger.trace("There is no RUs with requests - filterfarm problem");
					// all bus blocked or cloud
					int allBus = 0;
					int blockedOrCloud = 0;
					for (BU bu : daq.getBus()) {
						if ("cloud".equalsIgnoreCase(bu.getStateName())) {
							blockedOrCloud++;
						} else if ("blocked".equalsIgnoreCase(bu.getStateName())) {
							blockedOrCloud++;
						}
						allBus++;
					}
					if (allBus == blockedOrCloud) {
						// all bus fu hlt == 0

						int busWithFuHltZero = 0;
						for (BU bu : daq.getBus()) {
							if (0 == bu.getNumFUsHLT()) {
								busWithFuHltZero++;
							}
						}
						if (allBus == busWithFuHltZero) {
							return Subcase.HltProblem;
						}
					} else {
						return Subcase.BugInFilterfarm;
					}

					return Subcase.UnknownFilterfarmProblem;
				}
				logger.trace("There are other RUs with requests");

				logger.debug("#5 check: check if rus with requests are failed or syncloss");
				for (RU ruWithRequests : rusWithManyRequests) {
					Subcase result = checkProblemWithDataReceivedByRu(ruWithRequests);
					if (result != null) {
						return result;
					}
				}

				logger.debug("#6 check: check if ru has only 1 fed");
				for (RU ruWithRequests : rusWithManyRequests) {
					try {

						List<SubFEDBuilder> sfbs = ruWithRequests.getFedBuilder().getSubFedbuilders();
						if (sfbs.size() == 1) {
							List<FED> feds = sfbs.iterator().next().getFeds();
							int activeFeds = 0;
							for (FED fed : feds) {
								if (fed.isFmmMasked() || fed.isFrlMasked()) {
									// dont count masked feds
								} else {
									activeFeds++;
								}
							}
							if (activeFeds == 1) {
								return Subcase.SpecificFedBlocking;
							}
						}

					} catch (NullPointerException e) {
						// problem accessing the FEDs from RU
					}
				}

			}
		}
		return Subcase.Unknown;
	}

	private List<FED> notMaskedFedsFromRU(RU ru) {
		List<FED> result = new ArrayList<FED>();
		List<SubFEDBuilder> sfbsss = ru.getFedBuilder().getSubFedbuilders();
		for (SubFEDBuilder sfb : sfbsss) {
			try {
				List<FRL> frls = sfb.getFrls();
				for (FRL frl : frls) {
					try {
						Collection<FED> feds = frl.getFeds().values();
						for (FED fed : feds) {
							if (!fed.isFmmMasked() && !fed.isFrlMasked()) {
								result.add(fed);
							}
						}
					} catch (NullPointerException e) {
						// nothing to do: just skip missing
					}
				}
			} catch (NullPointerException e) {
				// nothing to do: just skip missing
			}
		}
		return result;

	}

	private FED findFEDinRUByFEDId(RU ru, int id) {
		List<FED> result = new ArrayList<FED>();
		List<SubFEDBuilder> sfbsss = ru.getFedBuilder().getSubFedbuilders();
		for (SubFEDBuilder sfb : sfbsss) {
			try {
				List<FRL> frls = sfb.getFrls();
				for (FRL frl : frls) {
					try {
						Collection<FED> feds = frl.getFeds().values();
						for (FED fed : feds) {
							if (fed.getSrcIdExpected() == id) {
								return fed;
							}
						}
					} catch (NullPointerException e) {
						// nothing to do: just skip missing
					}
				}
			} catch (NullPointerException e) {
				// nothing to do: just skip missing
			}
		}
		return null;
	}

	private Subcase checkProblemWithDataReceivedByRu(RU ru) {

		if ("Failed".equalsIgnoreCase(ru.getStateName())) {
			context.register("PROBLEM-RU", ru.getHostname());

			logger.trace(">Found failed RU " + ru.getHostname() + ", now will check for corrupted data");

			List<FED> notMaskedFedsOfRuWithRequests = notMaskedFedsFromRU(ru);
			for (FED fed : notMaskedFedsOfRuWithRequests) {
				if (fed.getRuFedDataCorruption() > 0) {
					logger.trace(">FED " + fed.getSrcIdExpected() + " has sent corrupted data "
							+ fed.getRuFedDataCorruption());
					context.register("PROBLEM-FED", fed.getSrcIdExpected());
					context.register("PROBLEM-TTCP", fed.getTtcp().getName());
					context.register("PROBLEM-SUBSYSTEM", fed.getTtcp().getSubsystem().getName());
				}
			}

			return Subcase.CorruptedDataReceived;
		} else if ("SyncLoss".equalsIgnoreCase(ru.getStateName())) {

			context.register("PROBLEM-RU", ru.getHostname());

			FED fed = null;
			Matcher mo = syncLossPattern.matcher(ru.getErrorMsg());
			if (mo.find()) {
				int fedId = Integer.parseInt(mo.group(1));
				context.register("PROBLEM-FED", fedId);
				fed = findFEDinRUByFEDId(ru, fedId);
			} else {
				for (FED f : notMaskedFedsFromRU(ru)) {
					if (f.getRuFedOutOfSync() > 0) {
						fed = f;
						context.register("PROBLEM-FED", f.getSrcIdExpected());
						break;
					}
				}
			}
			if (fed != null) {
				context.register("PROBLEM-TTCP", fed.getTtcp().getName());
				context.register("PROBLEM-SUBSYSTEM", fed.getTtcp().getSubsystem().getName());
			}
			return Subcase.OutOfSequenceDataReceived;
		}
		return null;
	}
}

enum Subcase {
	LinkProblem,
	WaitingForOtherFedsInFB,
	RuIsStuck,
	HltProblem,

	OutOfSequenceDataReceived,
	CorruptedDataReceived,
	SpecificFedBlocking,

	UnknownFilterfarmProblem,
	BugInFilterfarm,
	Unknown;
}
