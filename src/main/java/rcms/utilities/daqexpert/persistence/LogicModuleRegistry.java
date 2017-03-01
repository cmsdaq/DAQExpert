package rcms.utilities.daqexpert.persistence;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.Setting;
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
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase1;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase2;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase3;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase4;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase5;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase6;

public enum LogicModuleRegistry {

	NoRate(new NoRate(), ConditionGroup.NO_RATE, "Satisfied when no rate in DAQ fed builder summary"),
	RateOutOfRange(
			new RateOutOfRange(Integer.parseInt(Application.get().getProp(Setting.EXPERT_L1_RATE_MIN)),
					Integer.parseInt(Application.get().getProp(Setting.EXPERT_L1_RATE_MAX))),
			ConditionGroup.RATE_OUT_OF_RANGE,
			""),
	BeamActive(new BeamActive(), ConditionGroup.BEAM_ACTIVE, ""),
	RunOngoing(new RunOngoing(), ConditionGroup.RUN_ONGOING, ""),
	ExpectedRate(new ExpectedRate(), ConditionGroup.EXPECTED_RATE, ""),
	Transition(new Transition(), ConditionGroup.TRANSITION, ""),
	LongTransition(new LongTransition(), ConditionGroup.HIDDEN, ""),
	WarningInSubsystem(new WarningInSubsystem(), ConditionGroup.Warning, ""),
	SubsystemRunningDegraded(new SubsystemRunningDegraded(), ConditionGroup.SUBSYS_DEGRADED, ""),
	SubsystemError(new SubsystemError(), ConditionGroup.SUBSYS_ERROR, ""),
	SubsystemSoftError(new SubsystemSoftError(), ConditionGroup.SUBSYS_SOFT_ERR, ""),
	FEDDeadtime(
			new FEDDeadtime(Integer.parseInt(Application.get().getProp(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED))),
			ConditionGroup.FED_DEADTIME,
			""),
	PartitionDeadtime(
			new PartitionDeadtime(
					Integer.parseInt(Application.get().getProp(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_PARTITION))),
			ConditionGroup.PARTITION_DEADTIME,
			""),
	StableBeams(new StableBeams(), ConditionGroup.HIDDEN, ""),
	NoRateWhenExpected(new NoRateWhenExpected(), ConditionGroup.NO_RATE_WHEN_EXPECTED, ""),
	Downtime(new Downtime(), ConditionGroup.DOWNTIME, ""),
	Deadtime(
			new Deadtime(Integer.parseInt(Application.get().getProp(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL))),
			ConditionGroup.DEADTIME,
			""),
	CriticalDeadtime(new CriticalDeadtime(), ConditionGroup.CRITICAL_DEADTIME, ""),
	FlowchartCase1(new FlowchartCase1(), ConditionGroup.FLOWCHART, ""),
	FlowchartCase2(new FlowchartCase2(), ConditionGroup.FLOWCHART, ""),
	FlowchartCase3(new FlowchartCase3(), ConditionGroup.FLOWCHART, ""),
	FlowchartCase4(new FlowchartCase4(), ConditionGroup.FLOWCHART, ""),
	FlowchartCase5(new FlowchartCase5(), ConditionGroup.FLOWCHART, ""),
	FlowchartCase6(new FlowchartCase6(), ConditionGroup.FLOWCHART, ""),
	SessionComparator(new SessionComparator(), ConditionGroup.SESSION_NUMBER, ""),
	LHCBeamModeComparator(new LHCBeamModeComparator(), ConditionGroup.LHC_BEAM, ""),
	LHCMachineModeComparator(new LHCMachineModeComparator(), ConditionGroup.LHC_MACHINE, ""),
	RunComparator(new RunComparator(), ConditionGroup.RUN_NUMBER, ""),
	LevelZeroStateComparator(new LevelZeroStateComparator(), ConditionGroup.LEVEL_ZERO, ""),
	TCDSStateComparator(new TCDSStateComparator(), ConditionGroup.TCDS_STATE, ""),
	DAQStateComparator(new DAQStateComparator(), ConditionGroup.DAQ_STATE, "");

	private LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description) {
		this.logicModule = logicModule;
		this.description = description;
		this.group = group;
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

	public ConditionGroup getGroup() {
		return group;
	}

}
