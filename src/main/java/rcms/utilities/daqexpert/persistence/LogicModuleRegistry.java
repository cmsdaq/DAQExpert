package rcms.utilities.daqexpert.persistence;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.processing.OrderManager;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public enum LogicModuleRegistry {

    NoRate(rcms.utilities.daqexpert.reasoning.logic.basic.NoRate.class.getName(), ConditionGroup.NO_RATE, "Satisfied when no rate in DAQ fed builder summary", 10),
    RateOutOfRange(rcms.utilities.daqexpert.reasoning.logic.basic.RateOutOfRange.class.getName(), ConditionGroup.RATE_OUT_OF_RANGE, "", 9),
    BeamActive(rcms.utilities.daqexpert.reasoning.logic.basic.BeamActive.class.getName(), ConditionGroup.BEAM_ACTIVE, ""),
    RunOngoing(rcms.utilities.daqexpert.reasoning.logic.basic.RunOngoing.class.getName(), ConditionGroup.RUN_ONGOING, "", 100),
    ExpectedRate(rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate.class.getName(), ConditionGroup.EXPECTED_RATE, ""),
    Transition(null, ConditionGroup.TRANSITION, ""),
    LongTransition(rcms.utilities.daqexpert.reasoning.logic.basic.LongTransition.class.getName(), ConditionGroup.HIDDEN, ""),
    WarningInSubsystem(null, ConditionGroup.Warning, "Covered by other", 1004),
    SubsystemRunningDegraded(rcms.utilities.daqexpert.reasoning.logic.basic.SubsystemRunningDegraded.class.getName(), ConditionGroup.SUBSYS_DEGRADED, "", 1006),
    SubsystemError(rcms.utilities.daqexpert.reasoning.logic.basic.SubsystemError.class.getName(), ConditionGroup.SUBSYS_ERROR, "", 1007),
    SubsystemSoftError(rcms.utilities.daqexpert.reasoning.logic.basic.SubsystemSoftError.class.getName(), ConditionGroup.SUBSYS_SOFT_ERR, "", 1005),
    FEDDeadtime(rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime.class.getName(), ConditionGroup.FED_DEADTIME, "", 1005),
    PartitionDeadtime(rcms.utilities.daqexpert.reasoning.logic.basic.PartitionDeadtime.class.getName(), ConditionGroup.PARTITION_DEADTIME, "", 1008),
    StableBeams(rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams.class.getName(), ConditionGroup.HIDDEN, ""),
    NoRateWhenExpected(rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected.class.getName(), ConditionGroup.NO_RATE_WHEN_EXPECTED, "", 104),
    Downtime(rcms.utilities.daqexpert.reasoning.logic.basic.Downtime.class.getName(), ConditionGroup.DOWNTIME, ""),
    Deadtime(rcms.utilities.daqexpert.reasoning.logic.basic.Deadtime.class.getName(), ConditionGroup.DEADTIME, ""),
    CriticalDeadtime(rcms.utilities.daqexpert.reasoning.logic.basic.CriticalDeadtime.class.getName(), ConditionGroup.CRITICAL_DEADTIME, "", 105),
    FlowchartCase1(rcms.utilities.daqexpert.reasoning.logic.failures.LegacyFlowchartCase1.class.getName(), ConditionGroup.FLOWCHART, "Legacy OutOfSequenceData", 10004),
    FlowchartCase2(null, ConditionGroup.FLOWCHART, "Legacy CorruptedData. Covered by other", 10005),
    FlowchartCase3(rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase3.class.getName(), ConditionGroup.FLOWCHART, "", 10006),
    FlowchartCase4(null, ConditionGroup.FLOWCHART, "Partition disconnected: extended to other LMs", 0),
    FlowchartCase5(rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase5.class.getName(), ConditionGroup.FLOWCHART, "", 10008),
    FlowchartCase6(null, ConditionGroup.FLOWCHART, "Extended to multiple LMs", 10009),

    SessionComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.SessionComparator.class.getName(), ConditionGroup.SESSION_NUMBER, "Session", 15),
    LHCBeamModeComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.LHCBeamModeComparator.class.getName(), ConditionGroup.LHC_BEAM, "LHC Beam Mode", 20),
    LHCMachineModeComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.LHCMachineModeComparator.class.getName(), ConditionGroup.LHC_MACHINE, "LHC Machine Mode", 21),
    RunComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.RunComparator.class.getName(), ConditionGroup.RUN_NUMBER, "Run", 14),
    LevelZeroStateComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.LevelZeroStateComparator.class.getName(), ConditionGroup.LEVEL_ZERO, "Level Zero State", 13),
    TCDSStateComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.TCDSStateComparator.class.getName(), ConditionGroup.TCDS_STATE, "TCDS State", 12),

    DAQStateComparator(rcms.utilities.daqexpert.reasoning.logic.comparators.DAQStateComparator.class.getName(), ConditionGroup.DAQ_STATE, "DAQ state", 11),

    PiDisconnected(rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.PiDisconnected.class.getName(), ConditionGroup.FLOWCHART, "", 10014),
    PiProblem(rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.ProblemWithPi.class.getName(), ConditionGroup.FLOWCHART, "", 10014),
    FEDDisconnected(rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FEDDisconnected.class.getName(), ConditionGroup.FLOWCHART, "", 10014),
    FMMProblem(rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FMMProblem.class.getName(), ConditionGroup.FLOWCHART, "", 10014),
    UnidentifiedFailure(rcms.utilities.daqexpert.reasoning.logic.failures.UnidentifiedFailure.class.getName(), ConditionGroup.OTHER, "", 9000),

    FEROLFifoStuck(rcms.utilities.daqexpert.reasoning.logic.failures.FEROLFifoStuck.class.getName(), ConditionGroup.OTHER, "", 10500),

    RuFailed(rcms.utilities.daqexpert.reasoning.logic.failures.RuFailed.class.getName(), ConditionGroup.OTHER, "", 9500),


    LinkProblem(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.LinkProblem.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    RuStuckWaiting(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuckWaiting.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    RuStuck(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuck.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    RuStuckWaitingOther(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuckWaitingOther.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    HLTProblem(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.HLTProblem.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    BugInFilterfarm(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.BugInFilterfarm.class.getName(), ConditionGroup.FLOWCHART, "", 101),
    OnlyFedStoppedSendingData(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OnlyFedStoppedSendingData.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    OutOfSequenceData(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OutOfSequenceData.class.getName(), ConditionGroup.FLOWCHART, "", 10010),
    CorruptedData(rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.CorruptedData.class.getName(), ConditionGroup.FLOWCHART, "", 10010),

    RateTooHigh(rcms.utilities.daqexpert.reasoning.logic.failures.RateTooHigh.class.getName(), ConditionGroup.RATE_OUT_OF_RANGE, "Rate too high", 10501),

    ContinousSoftError(rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.ContinouslySoftError.class.getName(), ConditionGroup.OTHER, "", 1010),
    StuckAfterSoftError(rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.StuckAfterSoftError.class.getName(), ConditionGroup.OTHER, "", 1011),
    LengthyFixingSoftError(rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.LengthyFixingSoftError.class.getName(), ConditionGroup.OTHER, "", 1012),

    TTSDeadtime(rcms.utilities.daqexpert.reasoning.logic.basic.TTSDeadtime.class.getName(), ConditionGroup.CRITICAL_DEADTIME, "", 106),
    CloudFuNumber(rcms.utilities.daqexpert.reasoning.logic.basic.CloudFuNumber.class.getName(), ConditionGroup.OTHER, "Number of cloud FUs", 102),

    HltOutputBandwidthTooHigh(rcms.utilities.daqexpert.reasoning.logic.failures.HltOutputBandwidthTooHigh.class.getName(), ConditionGroup.OTHER, "", 2000),
    HltOutputBandwidthExtreme(rcms.utilities.daqexpert.reasoning.logic.failures.HltOutputBandwidthExtreme.class.getName(), ConditionGroup.OTHER, "", 2001),

    HighTcdsInputRate(rcms.utilities.daqexpert.reasoning.logic.failures.HighTcdsInputRate.class.getName(), ConditionGroup.OTHER, "", 3000),
    VeryHighTcdsInputRate(rcms.utilities.daqexpert.reasoning.logic.failures.VeryHighTcdsInputRate.class.getName(), ConditionGroup.OTHER, "", 3001),

    DeadtimeFromReTri(rcms.utilities.daqexpert.reasoning.logic.failures.DeadtimeFromReTri.class.getName(), ConditionGroup.OTHER, "", 3002),

    BackpressureFromFerol(rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromFerol.class.getName(), ConditionGroup.OTHER, "", 2000),
    BackpressureFromEventBuilding(rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromEventBuilding.class.getName(), ConditionGroup.OTHER, "", 2001),
    BackpressureFromHlt(rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.BackpressureFromHlt.class.getName(), ConditionGroup.OTHER, "", 2002),


    FedGeneratesDeadtime(rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.FedGeneratesDeadtime.class.getName(), ConditionGroup.OTHER, "", 2001),
    FedDeadtimeDueToDaq(rcms.utilities.daqexpert.reasoning.logic.failures.deadtime.FedDeadtimeDueToDaq.class.getName(), ConditionGroup.OTHER, "", 1050),
    CmsswCrashes(rcms.utilities.daqexpert.reasoning.logic.failures.CmsswCrashes.class.getName(), ConditionGroup.OTHER, "", 2012),
    TmpUpgradedFedProblem(rcms.utilities.daqexpert.reasoning.logic.basic.TmpUpgradedFedProblem.class.getName(), ConditionGroup.OTHER, "", 2012),
    HltCpuLoad(rcms.utilities.daqexpert.reasoning.logic.basic.HltCpuLoad.class.getName(), ConditionGroup.OTHER, "", 2013),
    FedStuckDueToDaq(rcms.utilities.daqexpert.reasoning.logic.failures.FedStuckDueToDaq.class.getName(), ConditionGroup.OTHER,"",2003);

    private LogicModule logicModule;

    public String getLogicModuleClassName() {
        return logicModuleClassName;
    }

    private final String logicModuleClassName;
    private final String description;
    private final ConditionGroup group;
    private final int usefulness;

    LogicModuleRegistry(String logicModuleClassName, ConditionGroup group, String description) {
        this(logicModuleClassName, group, description, 1);


        // note that this must be declared after all other logic modules
        // identifying an error condition in order to have UnidentifiedFailure
        // run after the others


    }
    LogicModuleRegistry(String logicModuleClassName, ConditionGroup group, String description, int usefulness) {
        this.logicModuleClassName = logicModuleClassName;
        this.description = description;
        this.group = group;
        this.usefulness = usefulness;
    }

    public static void init(){

        for (LogicModuleRegistry logicModuleRegistry : LogicModuleRegistry.values()) {
            if (logicModuleRegistry.getLogicModuleClassName() != null) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(logicModuleRegistry.getLogicModuleClassName());
                    Constructor<?> ctor = clazz.getConstructor();
                    LogicModule object = (LogicModule) ctor.newInstance();
                    logicModuleRegistry.setLogicModule(object);
                } catch (ClassNotFoundException | NoSuchMethodException |InstantiationException|IllegalAccessException |InvocationTargetException e) {
                    e.printStackTrace();
                    throw new ExpertException(ExpertExceptionCode.ExpertProblem, "Could not initialize the LM "
                            + logicModuleRegistry + " by classname: " + logicModuleRegistry.getLogicModuleClassName());
                }
            }

        }


        for (LogicModuleRegistry logicModuleRegistry : LogicModuleRegistry.values()) {
            if (logicModuleRegistry.getLogicModuleClassName() != null) {

                System.out.println(logicModuleRegistry + ": " + logicModuleRegistry.getLogicModule());

            }
        }
    }

    /**
     * @return the registered logic modules order in by increasing runOrder. Throws Error if there is more than one
     * module with the same run order.
     */
    public static List<LogicModule> getModulesInRunOrder() {

        init();

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

    public void setLogicModule(LogicModule logicModule) {
        this.logicModule = logicModule;
    }
}
