package rcms.utilities.daqexpert;

public enum Setting {

	NM_DASHBOARD("nm.dashboard"),
	NM_ARCHIVE("nm.archive"),
	NM_API_CREATE("nm.api"),
	NM_OFFSET("nm.offset"),
	DAQVIEW_LINK("daqview.link"),
	DAQVIEW_SETUP("daqview.setup"),

	EXPERT_L1_RATE_MIN("expert.l1.rate.min"),
	EXPERT_L1_RATE_MAX("expert.l1.rate.max"),

	EXPERT_LOGIC_DEADTIME_THESHOLD_FED("expert.logic.deadtime.threshold.fed"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_PARTITION("expert.logic.deadtime.threshold.partition"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL("expert.logic.deadtime.threshold.total"),

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

	;

	private final String key;

	private final boolean required;

	private Setting(String key) {
		this.key = key;
		this.required = true;
	}

	public boolean isRequired() {
		return required;
	}

	public String getKey() {
		return key;
	}
}
