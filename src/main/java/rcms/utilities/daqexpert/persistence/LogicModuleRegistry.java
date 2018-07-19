package rcms.utilities.daqexpert.persistence;

import rcms.utilities.daqexpert.processing.OrderManager;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.logic.basic.*;
import rcms.utilities.daqexpert.reasoning.logic.comparators.*;
import rcms.utilities.daqexpert.reasoning.logic.failures.*;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.*;
import rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.*;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FEDDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FMMProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.PiDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.ProblemWithPi;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.ContinouslySoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.LengthyFixingSoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.StuckAfterSoftError;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public enum LogicModuleRegistry {

    NoRate(new NoRate(), ConditionGroup.NO_RATE, "Satisfied when no rate in DAQ fed builder summary", 10),
    RateOutOfRange(new RateOutOfRange(), ConditionGroup.RATE_OUT_OF_RANGE, "", 9),
    BeamActive(new BeamActive(), ConditionGroup.BEAM_ACTIVE, ""),
    RunOngoing(new RunOngoing(), ConditionGroup.RUN_ONGOING, "", 100),
    ExpectedRate(new ExpectedRate(), ConditionGroup.EXPECTED_RATE, ""),
    Transition(null, ConditionGroup.TRANSITION, ""),
    LongTransition(new LongTransition(), ConditionGroup.HIDDEN, ""),
    WarningInSubsystem(null, ConditionGroup.Warning, "Covered by other", 1004),
    SubsystemRunningDegraded(new SubsystemRunningDegraded(), ConditionGroup.SUBSYS_DEGRADED, "", 1006),
    SubsystemError(new SubsystemError(), ConditionGroup.SUBSYS_ERROR, "", 1007),
    SubsystemSoftError(new SubsystemSoftError(), ConditionGroup.SUBSYS_SOFT_ERR, "", 1005),
    FEDDeadtime(new FEDDeadtime(), ConditionGroup.FED_DEADTIME, "", 1005),
    PartitionDeadtime(new PartitionDeadtime(), ConditionGroup.PARTITION_DEADTIME, "", 1008),
    StableBeams(new StableBeams(), ConditionGroup.HIDDEN, ""),
    NoRateWhenExpected(new NoRateWhenExpected(), ConditionGroup.NO_RATE_WHEN_EXPECTED, "", 104),
    Downtime(new Downtime(), ConditionGroup.DOWNTIME, ""),
    Deadtime(new Deadtime(), ConditionGroup.DEADTIME, ""),
    CriticalDeadtime(new CriticalDeadtime(), ConditionGroup.CRITICAL_DEADTIME, "", 105),
    FlowchartCase1(new LegacyFlowchartCase1(), ConditionGroup.FLOWCHART, "Legacy OutOfSequenceData", 10004),
    FlowchartCase2(null, ConditionGroup.FLOWCHART, "Legacy CorruptedData. Covered by other", 10005),
    FlowchartCase3(new FlowchartCase3(), ConditionGroup.FLOWCHART, "", 10006),
    FlowchartCase4(null, ConditionGroup.FLOWCHART, "Partition disconnected: extended to other LMs", 0),
    FlowchartCase5(new FlowchartCase5(), ConditionGroup.FLOWCHART, "", 10008),
    FlowchartCase6(null, ConditionGroup.FLOWCHART, "Extended to multiple LMs", 10009),

    SessionComparator(new SessionComparator(), ConditionGroup.SESSION_NUMBER, "Session", 15),
    LHCBeamModeComparator(new LHCBeamModeComparator(), ConditionGroup.LHC_BEAM, "LHC Beam Mode", 20),
    LHCMachineModeComparator(new LHCMachineModeComparator(), ConditionGroup.LHC_MACHINE, "LHC Machine Mode", 21),
    RunComparator(new RunComparator(), ConditionGroup.RUN_NUMBER, "Run", 14),
    LevelZeroStateComparator(new LevelZeroStateComparator(), ConditionGroup.LEVEL_ZERO, "Level Zero State", 13),
    TCDSStateComparator(new TCDSStateComparator(), ConditionGroup.TCDS_STATE, "TCDS State", 12),

    DAQStateComparator(new DAQStateComparator(), ConditionGroup.DAQ_STATE, "DAQ state", 11),

    PiDisconnected(new PiDisconnected(), ConditionGroup.FLOWCHART, "", 10014),
    PiProblem(new ProblemWithPi(), ConditionGroup.FLOWCHART, "", 10014),
    FEDDisconnected(new FEDDisconnected(), ConditionGroup.FLOWCHART, "", 10014),
    FMMProblem(new FMMProblem(), ConditionGroup.FLOWCHART, "", 10014),
    UnidentifiedFailure(new UnidentifiedFailure(), ConditionGroup.OTHER, "", 9000),

    FEROLFifoStuck(new FEROLFifoStuck(), ConditionGroup.OTHER, "", 10500),

    RuFailed(new RuFailed(), ConditionGroup.OTHER, "", 9500),


    LinkProblem(new LinkProblem(), ConditionGroup.FLOWCHART, "", 10010),
    RuStuckWaiting(new RuStuckWaiting(), ConditionGroup.FLOWCHART, "", 10010),
    RuStuck(new RuStuck(), ConditionGroup.FLOWCHART, "", 10010),
    RuStuckWaitingOther(new RuStuckWaitingOther(), ConditionGroup.FLOWCHART, "", 10010),
    HLTProblem(new HLTProblem(), ConditionGroup.FLOWCHART, "", 10010),
    BugInFilterfarm(new BugInFilterfarm(), ConditionGroup.FLOWCHART, "", 101),
    OnlyFedStoppedSendingData(new OnlyFedStoppedSendingData(), ConditionGroup.FLOWCHART, "", 10010),
    OutOfSequenceData(new OutOfSequenceData(), ConditionGroup.FLOWCHART, "", 10010),
    CorruptedData(new CorruptedData(), ConditionGroup.FLOWCHART, "", 10010),

    RateTooHigh(new RateTooHigh(), ConditionGroup.RATE_OUT_OF_RANGE, "Rate too high", 10501),

    ContinousSoftError(new ContinouslySoftError(), ConditionGroup.OTHER, "", 1010),
    StuckAfterSoftError(new StuckAfterSoftError(), ConditionGroup.OTHER, "", 1011),
    LengthyFixingSoftError(new LengthyFixingSoftError(), ConditionGroup.OTHER, "", 1012),

    TTSDeadtime(new TTSDeadtime(), ConditionGroup.CRITICAL_DEADTIME, "", 106),
    CloudFuNumber(new CloudFuNumber(), ConditionGroup.OTHER, "Number of cloud FUs", 102),

    HltOutputBandwidthTooHigh(new HltOutputBandwidthTooHigh(), ConditionGroup.OTHER, "", 2000),
    HltOutputBandwidthExtreme(new HltOutputBandwidthExtreme(), ConditionGroup.OTHER, "", 2001),

    HighTcdsInputRate(new HighTcdsInputRate(), ConditionGroup.OTHER, "", 3000),
    VeryHighTcdsInputRate(new VeryHighTcdsInputRate(), ConditionGroup.OTHER, "", 3001),

    DeadtimeFromReTri(new DeadtimeFromReTri(), ConditionGroup.OTHER, "", 3002),

    BackpressureFromFerol(new BackpressureFromFerol(), ConditionGroup.OTHER, "", 2000),
    BackpressureFromEventBuilding(new BackpressureFromEventBuilding(), ConditionGroup.OTHER, "", 2001),
    BackpressureFromHlt(new BackpressureFromHlt(), ConditionGroup.OTHER, "", 2002),


    FedGeneratesDeadtime(new FedGeneratesDeadtime(), ConditionGroup.OTHER, "", 2001),
    FedDeadtimeDueToDaq(new FedDeadtimeDueToDaq(), ConditionGroup.OTHER, "", 1050),
    CmsswCrashes(new CmsswCrashes(), ConditionGroup.OTHER, "", 2012),
    TmpUpgradedFedProblem(new TmpUpgradedFedProblem(), ConditionGroup.OTHER, "", 2012),
    HltCpuLoad(new HltCpuLoad(), ConditionGroup.OTHER, "", 2013),
    FedStuckDueToDaq(new FedStuckDueToDaq(), ConditionGroup.OTHER,"",2003);

    private final LogicModule logicModule;
    private final String description;
    private final ConditionGroup group;
    private final int usefulness;

    LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description) {
        this(logicModule, group, description, 1);


        // note that this must be declared after all other logic modules
        // identifying an error condition in order to have UnidentifiedFailure
        // run after the others


    }
    LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description, int usefulness) {
        this.logicModule = logicModule;
        this.description = description;
        this.group = group;
        this.usefulness = usefulness;
    }

    /**
     * @return the registered logic modules order in by increasing runOrder. Throws Error if there is more than one
     * module with the same run order.
     */
    public static List<LogicModule> getModulesInRunOrder() {

        OrderManager orderManager = new OrderManager();

        Set<LogicModule> toOrder = new LinkedHashSet<>();
        for (LogicModuleRegistry logicModuleRegistry : LogicModuleRegistry.values()) {
            if (logicModuleRegistry.getLogicModule() != null) {
                logicModuleRegistry.getLogicModule().setLogicModuleRegistry(logicModuleRegistry);
                logicModuleRegistry.getLogicModule().declareRelations();
                toOrder.add(logicModuleRegistry.getLogicModule());
            }
        }

        return orderManager.order(toOrder);
    }

    public LogicModule getLogicModule() {
        return logicModule;
    }

    public String getDescription() {
        return description;
    }

    public ConditionGroup getGroup() {
        return group;
    }

    public int getUsefulness() {
        return usefulness;
    }

}
