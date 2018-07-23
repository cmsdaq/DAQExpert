package rcms.utilities.daqexpert.reasoning.base.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConditionGroup {

	LHC_BEAM("lhc-beam"),
	LHC_MACHINE("lhc-machine"),
	BEAM_ACTIVE("beam-active"),
	NO_RATE("no-rate"),
	DOWNTIME("dt"),
	DEADTIME("deadtime"),
	CRITICAL_DEADTIME("critical-deadtime"),
	FED_DEADTIME("feddead"),
	PARTITION_DEADTIME("partition-dead"),
	AVOIDABLE_DOWNTIME("adt"),
	RATE_OUT_OF_RANGE("rate-oor"),
	Warning("warning"),
	NO_RATE_WHEN_EXPECTED("nrwe"),
	RUN_NUMBER("run-no"),
	EXPECTED_RATE("expected"),
	RUN_ONGOING("run-on"),
	TRANSITION("transition"),
	SESSION_NUMBER("session-no"),
	FLOWCHART("flowchart"),
	LEVEL_ZERO("level-zero"),
	TCDS_STATE("tcds"),
	DAQ_STATE("daq-state"),
	EXPERIMENTAL("experimental"),
	OTHER("other"),
	SUBSYS_DEGRADED("ssdegraded"),
	SUBSYS_SOFT_ERR("ss-soft-err"),
	SUBSYS_ERROR("ss-err"),
	EXPERT_VERSION("ver"),
	RECOVERIES("rec"),
	HIDDEN("hidden"),
	DOMINATING("dominating");
	
	private final String code;

	private final ConditionPriority defaultPriority;

	private ConditionGroup(String code, ConditionPriority defaultPriority) {
		this.code = code;
		this.defaultPriority = defaultPriority;
	}

	private ConditionGroup(String code) {
		this(code, ConditionPriority.DEFAULTT);
	}

	@JsonValue
	public String getCode() {
		return code;
	}

	public ConditionPriority getDefaultPriority() {
		return defaultPriority;
	}
}
