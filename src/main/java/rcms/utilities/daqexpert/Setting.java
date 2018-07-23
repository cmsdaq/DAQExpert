package rcms.utilities.daqexpert;

public enum Setting {

	NM_DASHBOARD("nm.dashboard"),
	NM_ARCHIVE("nm.archive"),
	NM_API_CREATE("nm.api"),
	NM_OFFSET("nm.offset"),

	CONTROLLER_ENABLED("controller.enabled"),
	CONTROLLER_URL("controller.address"),
	CONTROLLER_SOCKET("controller.socket"),

	DAQVIEW_LINK("daqview.link"),
	DAQVIEW_SETUP("daqview.setup"),

	EXPERT_L1_RATE_MIN("expert.l1.rate.min"),
	EXPERT_L1_RATE_MAX("expert.l1.rate.max"),
	EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH("expert.hlt.output.bandwidth.high"),
	EXPERT_HLT_OUTPUT_BANDWITH_EXTREME("expert.hlt.output.bandwidth.extreme"),
	EXPERT_CMSSW_CRASHES_THRESHOLD("expert.cmssw.crashes.increase"),
	EXPERT_CMSSW_CRASHES_TIME_WINDOW("expert.cmssw.crashes.timewindow.seconds"),

	/** thresholds for TCDS input rate checks */
	EXPERT_TCDS_INPUT_RATE_HIGH("expert.tcds.input.rate.high"),
	EXPERT_TCDS_INPUT_RATE_VERYHIGH("expert.tcds.input.rate.veryhigh"),
	
	EXPERT_LOGIC_DEADTIME_THESHOLD_FED("expert.logic.deadtime.threshold.fed"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_PARTITION("expert.logic.deadtime.threshold.partition"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL("expert.logic.deadtime.threshold.total"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_TTS("expert.logic.deadtime.threshold.tts"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_RETRI("expert.logic.deadtime.threshold.retri"),

	EXPERT_LOGIC_NO_TRIGGER_RATE_THRESHOLD("expert.logic.norate.threshold"),
	
	EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_COUNT("expert.logic.continoussofterror.threshold.count"),
	EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_PERIOD("expert.logic.continoussofterror.threshold.period"),
	EXPERT_LOGIC_CONTINOUSSOFTERROR_THESHOLD_KEEP("expert.logic.continoussofterror.threshold.keep"),
	EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD("expert.logic.lenghtyfixingsofterror.threshold.period"),

	/** maximum fraction of FUs in cloud mode around stable beams */
	EXPERT_LOGIC_CLOUDFUNUMBER_THRESHOLD_TOTAL_FRACTION("expert.logic.cloudfunumber.threshold.total.fraction"),

	/** period in milliseconds after the appearance of the first non-cloud LHC beam mode
	    after which a problem with too many FUs in cloud mode will be reported. */
	EXPERT_LOGIC_CLOUDFUNUMBER_HOLDOFF_PERIOD("expert.logic.cloudfunumber.holdoff.period"),

	EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED("expert.logic.deadtimeanalysis.fed.backpressure.threshold"),
	EXPERT_LOGIC_BACKPRESSUREFROMHLT_THRESHOLD_BUS("expert.logic.backpressurefromhlt.bus.enabled.threshold.fraction"),
	EXPERT_LOGIC_EVM_FEW_EVENTS("expert.logic.evm.requests.few.max"),

	/** threshold (range 0..1) above which the HLT CPU load is considered to be high */
	EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD("expert.logic.hlt.cpu.load.threshold"),

	/** holdoff period in milliseconds after the beginning of a run before
	 *  asserting the high hlt cpu load condition */
	EXPERT_LOGIC_HLT_CPU_LOAD_RUNONGOING_HOLDOFF_PERIOD("expert.logic.hlt.cpu.load.runongoing.holdoff.period"),
	EXPERT_LOGIC_HLT_CPU_LOAD_SELF_HOLDOFF_PERIOD("expert.logic.hlt.cpu.load.self.holdoff.period"),
	
	PROCESSING_START_DATETIME("processing.start"),
	PROCESSING_END_DATETIME("processing.end"),
	SNAPSHOTS_DIR("snapshots"),
	LANDING("landing"),
	EXPERIMENTAL_DIR("experimental"),

	DATABASE_USER("hibernate.connection.username"),
	DATABASE_PASSWORD("hibernate.connection.password"),
	DATABASE_URL("hibernate.connection.url"),
	DATABASE_DRIVER("hibernate.connection.driver_class"),
	DATABASE_MODE("hibernate.hbm2ddl.auto"),

	MARKUP_ENABLED("markup.enabled",false),
    PROBLEM_ESTABLISHED("expert.logic.problem-established.threshold"),
	DATAFLOW_ESTABLISHED("expert.logic.dataflow-established.threshold");

	private final String key;

	private final boolean required;

	private Setting(String key) {
		this.key = key;
		this.required = true;
	}

	private Setting(String key, Boolean required) {
		this.key = key;
		this.required = required;
	}
	public boolean isRequired() {
		return required;
	}

	public String getKey() {
		return key;
	}
}
