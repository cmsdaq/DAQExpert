package rcms.utilities.daqexpert.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.reasoning.base.LogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.logic.basic.BeamActive;
import rcms.utilities.daqexpert.reasoning.logic.basic.CloudFuNumber;
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
import rcms.utilities.daqexpert.reasoning.logic.failures.FEROLFifoStuck;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase3;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCase5;
import rcms.utilities.daqexpert.reasoning.logic.failures.LegacyFlowchartCase1;
import rcms.utilities.daqexpert.reasoning.logic.failures.LegacyFlowchartCase2;
import rcms.utilities.daqexpert.reasoning.logic.failures.RateTooHigh;
import rcms.utilities.daqexpert.reasoning.logic.failures.RuFailed;
import rcms.utilities.daqexpert.reasoning.logic.failures.UnidentifiedFailure;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.BugInFilterfarm;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.CorruptedData;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.HLTProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.LinkProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OnlyFedStoppedSendingData;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.OutOfSequenceData;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuck;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuckWaiting;
import rcms.utilities.daqexpert.reasoning.logic.failures.backpressure.RuStuckWaitingOther;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FEDDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.FMMProblem;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.PiDisconnected;
import rcms.utilities.daqexpert.reasoning.logic.failures.disconnected.ProblemWithPi;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.ContinouslySoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.LengthyFixingSoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.StuckAfterSoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.ContinouslySoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.LengthyFixingSoftError;
import rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors.StuckAfterSoftError;

public enum LogicModuleRegistry {

	NoRate                  (new NoRate(),                   ConditionGroup.NO_RATE,               "Satisfied when no rate in DAQ fed builder summary",   1,    10),
	RateOutOfRange          (new RateOutOfRange(),           ConditionGroup.RATE_OUT_OF_RANGE,     "",                                                    2,     9),
	BeamActive              (new BeamActive(),               ConditionGroup.BEAM_ACTIVE,           "",                                                    3),
	RunOngoing              (new RunOngoing(),               ConditionGroup.RUN_ONGOING,           "",                                                    4,   100),
	ExpectedRate            (new ExpectedRate(),             ConditionGroup.EXPECTED_RATE,         "",                                                    5),
	Transition              (new Transition(),               ConditionGroup.TRANSITION,            "",                                                    6),
	LongTransition          (new LongTransition(),           ConditionGroup.HIDDEN,                "",                                                    7),
	WarningInSubsystem      (new WarningInSubsystem(),       ConditionGroup.Warning,               "",                                                    8,  1004),
	SubsystemRunningDegraded(new SubsystemRunningDegraded(), ConditionGroup.SUBSYS_DEGRADED,       "",                                                    9,  1006),
	SubsystemError          (new SubsystemError(),           ConditionGroup.SUBSYS_ERROR,          "",                                                   10,  1007),
	SubsystemSoftError      (new SubsystemSoftError(),       ConditionGroup.SUBSYS_SOFT_ERR,       "",                                                   11,  1005),
	FEDDeadtime             (new FEDDeadtime(),              ConditionGroup.FED_DEADTIME,          "",                                                   12,  1005),
	PartitionDeadtime       (new PartitionDeadtime(),        ConditionGroup.PARTITION_DEADTIME,    "",                                                   13,  1008),
	StableBeams             (new StableBeams(),              ConditionGroup.HIDDEN,                "",                                                   14),
	NoRateWhenExpected      (new NoRateWhenExpected(),       ConditionGroup.NO_RATE_WHEN_EXPECTED, "",                                                   15,   104),
	Downtime                (new Downtime(),                 ConditionGroup.DOWNTIME,              "",                                                   16),
	Deadtime                (new Deadtime(),                 ConditionGroup.DEADTIME,              "",                                                   17),
	CriticalDeadtime        (new CriticalDeadtime(),         ConditionGroup.CRITICAL_DEADTIME,     "",                                                   18,   105),
	FlowchartCase1          (new LegacyFlowchartCase1(),     ConditionGroup.FLOWCHART,             "Legacy OutOfSequenceData",                     997, 10004),
	FlowchartCase2          (new LegacyFlowchartCase2(),     ConditionGroup.FLOWCHART,             "Legacy CorruptedData",                         998, 10005),
	FlowchartCase3          (new FlowchartCase3(),           ConditionGroup.FLOWCHART,             "",                                                   21, 10006),
	FlowchartCase4          (null,                           ConditionGroup.FLOWCHART,             "Partition disconnected: extended to other LMs",      22,     0),
	FlowchartCase5          (new FlowchartCase5(),           ConditionGroup.FLOWCHART,             "",                                                   23, 10008),
	FlowchartCase6          (null,                           ConditionGroup.FLOWCHART,             "Extended to multiple LMs",                           -24, 10009),

	SessionComparator       (new SessionComparator(),        ConditionGroup.SESSION_NUMBER,        "Session",                                            25,    15),
	LHCBeamModeComparator   (new LHCBeamModeComparator(),    ConditionGroup.LHC_BEAM,              "LHC Beam Mode",                                      26,    20),
	LHCMachineModeComparator(new LHCMachineModeComparator(), ConditionGroup.LHC_MACHINE,           "LHC Machine Mode",                                   27,    21),
	RunComparator           (new RunComparator(),            ConditionGroup.RUN_NUMBER,            "Run",                                                28,    14),
	LevelZeroStateComparator(new LevelZeroStateComparator(), ConditionGroup.LEVEL_ZERO,            "Level Zero State",                                   29,    13),
	TCDSStateComparator     (new TCDSStateComparator(),      ConditionGroup.TCDS_STATE,            "TCDS State",                                         30,    12),

	DAQStateComparator      (new DAQStateComparator(),       ConditionGroup.DAQ_STATE,             "DAQ state",                                          31,    11),

	PiDisconnected          (new PiDisconnected(),           ConditionGroup.FLOWCHART,             "",                                                   32, 10014),
	PiProblem               (new ProblemWithPi(),            ConditionGroup.FLOWCHART,             "",                                                   33, 10014),
	FEDDisconnected         (new FEDDisconnected(),          ConditionGroup.FLOWCHART,             "",                                                   34, 10014),
	FMMProblem              (new FMMProblem(),               ConditionGroup.FLOWCHART,             "",                                                   35, 10014),
	UnidentifiedFailure		(new UnidentifiedFailure(),		 ConditionGroup.OTHER,                 "",                                                  999, 9000),

	FEROLFifoStuck		   		(new FEROLFifoStuck(),		       ConditionGroup.OTHER,                 "",                                                  500,  10500),

	RuFailed                (new RuFailed(),                 ConditionGroup.OTHER,                 "",                                                   36,   9500),



	LinkProblem				(new LinkProblem(),				 ConditionGroup.FLOWCHART,             "",                                                   37, 10010),
	RuStuckWaiting			(new RuStuckWaiting(),			 ConditionGroup.FLOWCHART,             "",                                                   38, 10010),
	RuStuck					(new RuStuck(),					 ConditionGroup.FLOWCHART,             "",                                                   39, 10010),
	RuStuckWaitingOther		(new RuStuckWaitingOther(),		 ConditionGroup.FLOWCHART,             "",                                                   40, 10010),
	HLTProblem				(new HLTProblem(),				 ConditionGroup.FLOWCHART,             "",                                                   41, 10010),
	BugInFilterfarm			(new BugInFilterfarm(),			 ConditionGroup.FLOWCHART,             "",                                                   42, 10010),
	OnlyFedStoppedSendingData(new OnlyFedStoppedSendingData(),ConditionGroup.FLOWCHART,            "",                                                   43, 10010),
	OutOfSequenceData       (new OutOfSequenceData(),        ConditionGroup.FLOWCHART,             "",                                                   19, 10010),
	CorruptedData           (new CorruptedData(),            ConditionGroup.FLOWCHART,            "",                                                    20, 10010),

	RateTooHigh            (new RateTooHigh(),            ConditionGroup.RATE_OUT_OF_RANGE,         "Rate too high",                                     44, 10501),

    ContinousSoftError		(new ContinouslySoftError(),     ConditionGroup.OTHER,                 "",                                                  45,  1010),
    StuckAfterSoftError    	(new StuckAfterSoftError(),      ConditionGroup.OTHER,                 "",                                                  46,  1011),
    LengthyFixingSoftError 	(new LengthyFixingSoftError(),   ConditionGroup.OTHER,                 "",                                                  47,  1012),

	CloudFuNumber          (new CloudFuNumber(),             ConditionGroup.OTHER,                  "Number of cloud FUs",                               48),

    ;
	
	private LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description, int runOrder) {
		this(logicModule, group, description, runOrder, 1);

	
	// note that this must be declared after all other logic modules
	// identifying an error condition in order to have UnidentifiedFailure
	// run after the others
	

	}

	private LogicModuleRegistry(LogicModule logicModule, ConditionGroup group, String description, int runOrder, int usefulness) {
		this.logicModule = logicModule;
		this.description = description;
		this.group = group;
		this.usefulness = usefulness;
		
		// in principle we would like to keep a static map of run order -> logic module
		// here to check for duplicate runOrder values. But we can't access 
		// static fields which need initialization from this initializer, 
		// see http://stackoverflow.com/questions/9098862
		this.runOrder = runOrder;
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

	/** order for running the logic modules. */
	private final int runOrder;
	
	public ConditionGroup getGroup() {
		return group;
	}

	public int getUsefulness() {
		return usefulness;
	}

	/** @return the registered logic modules order in by increasing runOrder.
	 *  Throws Error if there is more than one module with the same run order.
	 */ 
	public static List<LogicModuleRegistry> getModulesInRunOrder() {

		SortedMap<Integer, LogicModuleRegistry> orderedModules = new TreeMap<Integer, LogicModuleRegistry>();
		
		for (LogicModuleRegistry lmr : LogicModuleRegistry.values()) {
			
			if (orderedModules.containsKey(lmr.runOrder)) {
				throw new ExpertException(ExpertExceptionCode.LogicModuleMisconfiguration, "runOrder " + lmr.runOrder + " found more than once: in " +
						orderedModules.get(lmr.runOrder).logicModule.getName() + " and in " +
						lmr.logicModule.getName()
		        );
			}
			
			orderedModules.put(lmr.runOrder, lmr);
			
		} // loop over logic modules
		
		return new ArrayList<LogicModuleRegistry>(orderedModules.values());
	}

}
