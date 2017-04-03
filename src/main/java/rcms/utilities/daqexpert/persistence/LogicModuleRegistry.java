package rcms.utilities.daqexpert.persistence;

import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.logic.basic.BeamActive;
import rcms.utilities.daqexpert.reasoning.logic.basic.CriticalDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Deadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.Downtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.ExpectedRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.FEDDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.LongTransition;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRate;
import rcms.utilities.daqexpert.reasoning.logic.basic.NoRateWhenExpected;
import rcms.utilities.daqexpert.reasoning.logic.basic.PartitionDeadtime;
import rcms.utilities.daqexpert.reasoning.logic.basic.RateOutOfRange;
import rcms.utilities.daqexpert.reasoning.logic.basic.RunOngoing;
import rcms.utilities.daqexpert.reasoning.logic.basic.StableBeams;
import rcms.utilities.daqexpert.reasoning.logic.basic.SubsystemError;
import rcms.utilities.daqexpert.reasoning.logic.basic.SubsystemRunningDegraded;
import rcms.utilities.daqexpert.reasoning.logic.basic.SubsystemSoftError;
import rcms.utilities.daqexpert.reasoning.logic.basic.Transition;
import rcms.utilities.daqexpert.reasoning.logic.basic.WarningInSubsystem;
import rcms.utilities.daqexpert.reasoning.logic.comparators.DAQStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LHCBeamModeComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LHCMachineModeComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.LevelZeroStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.RunComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.SessionComparator;
import rcms.utilities.daqexpert.reasoning.logic.comparators.TCDSStateComparator;
import rcms.utilities.daqexpert.reasoning.logic.failures.BugInFilterfarm;
import rcms.utilities.daqexpert.reasoning.logic.failures.CorruptedData;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase3;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase4;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase5;
import rcms.utilities.daqexpert.reasoning.logic.failures.HLTProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.LinkProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.OnlyFedStoppedSendingData;
import rcms.utilities.daqexpert.reasoning.logic.failures.OutOfSequenceData;
import rcms.utilities.daqexpert.reasoning.logic.failures.RuStuck;
import rcms.utilities.daqexpert.reasoning.logic.failures.RuStuckWaiting;

public enum LogicModuleRegistry {

	NoRate(new NoRate(), ConditionGroup.NO_RATE, "Satisfied when no rate in DAQ fed builder summary", 10),
	RateOutOfRange(new RateOutOfRange(), ConditionGroup.RATE_OUT_OF_RANGE, "", 9),
	BeamActive(new BeamActive(), ConditionGroup.BEAM_ACTIVE, ""),
	RunOngoing(new RunOngoing(), ConditionGroup.RUN_ONGOING, "", 100),
	ExpectedRate(new ExpectedRate(), ConditionGroup.EXPECTED_RATE, ""),
	Transition(new Transition(), ConditionGroup.TRANSITION, ""),
	LongTransition(new LongTransition(), ConditionGroup.HIDDEN, ""),
	WarningInSubsystem(new WarningInSubsystem(), ConditionGroup.Warning, "", 1004),
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
	FlowchartCase1(new OutOfSequenceData(), ConditionGroup.FLOWCHART, "", 10004),
	FlowchartCase2(new CorruptedData(), ConditionGroup.FLOWCHART, "", 10005),
	FlowchartCase3(new FlowchartCase3(), ConditionGroup.FLOWCHART, "", 10006),
	FlowchartCase4(new FlowchartCase4(), ConditionGroup.FLOWCHART, "", 10007),
	FlowchartCase5(new FlowchartCase5(), ConditionGroup.FLOWCHART, "", 10008),
	FlowchartCase6(null, ConditionGroup.FLOWCHART, "", 10009),
	SessionComparator(new SessionComparator(), ConditionGroup.SESSION_NUMBER, "Session", 15),
	LHCBeamModeComparator(new LHCBeamModeComparator(), ConditionGroup.LHC_BEAM, "LHC Beam Mode", 20),
	LHCMachineModeComparator(new LHCMachineModeComparator(), ConditionGroup.LHC_MACHINE, "LHC Machine Mode", 21),
	RunComparator(new RunComparator(), ConditionGroup.RUN_NUMBER, "Run", 14),
	LevelZeroStateComparator(new LevelZeroStateComparator(), ConditionGroup.LEVEL_ZERO, "Level Zero State", 13),
	TCDSStateComparator(new TCDSStateComparator(), ConditionGroup.TCDS_STATE, "TCDS State", 12),
	DAQStateComparator(new DAQStateComparator(), ConditionGroup.DAQ_STATE, "DAQ state", 11),
	LinkProblem(new LinkProblem(), ConditionGroup.FLOWCHART, "", 10010),
	RuStuckWaiting(new RuStuckWaiting(), ConditionGroup.FLOWCHART, "", 10011),
	RuStuck(new RuStuck(), ConditionGroup.FLOWCHART, "", 10012),
	HLTProblem(new HLTProblem(), ConditionGroup.FLOWCHART, "", 10013),
	BugInFilterfarm(new BugInFilterfarm(), ConditionGroup.FLOWCHART, "", 10013),
	OnlyFedStoppedSendingData(new OnlyFedStoppedSendingData(), ConditionGroup.FLOWCHART, "", 10013),;

	private LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description) {
		this(logicModule, group, description, 1);
	}

	private LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description, int usefulness) {
		this.logicModule = logicModule;
		this.description = description;
		this.group = group;
		this.usefulness = usefulness;
	}

	public LogicModule getLogicModule() {
		return logicModule;
	}

	public String getDescription() {
		return description;
	}

	private final LogicModule logicModule;
	private final String description;
	private final ConditionGroup group;
	private final int usefulness;

	public ConditionGroup getGroup() {
		return group;
	}

	public int getUsefulness() {
		return usefulness;
	}

}
