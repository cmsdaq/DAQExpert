package rcms.utilities.daqexpert.reasoning.base.enums;

public enum EventGroup {
	LHC_BEAM("lhc-beam",0),
	LHC_MACHINE("lhc-machine",0),
	NO_RATE("no-rate",0),
	DOWNTIME("dt",0),
	DEADTIME("deadtime",0),
	AVOIDABLE_DOWNTIME("adt",0),
	RATE_OUT_OF_RANGE("rate-oor",0),
	Warning("warning",0),
	NO_RATE_WHEN_EXPECTED("nrwe",2),
	RUN_NUMBER("run-no",0),
	RUN_ONGOING("run-on",0),
	SESSION_NUMBER("session-no",0),
	FLOWCHART("flowchart",1),
	LEVEL_ZERO("level-zero",0),
	DAQ_STATE("daq-state",0),
	EXPERIMENTAL("experimental",0),
	OTHER("other",0),
	HIDDEN("hidden",0);

	private final String code;
	
	private final int nmId;

	private EventGroup(String code, int nmId) {
		this.code = code;
		this.nmId= nmId;
	}

	public String getCode() {
		return code;
	}

	public int getNmId() {
		return nmId;
	}

}
