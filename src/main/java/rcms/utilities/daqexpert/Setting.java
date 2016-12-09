package rcms.utilities.daqexpert;

public enum Setting {

	NM_DASHBOARD("nm.dashboard"),
	NM_NOTIFICATIONS("nm.notifications"),
	NM_API_CREATE("nm.api.create"),
	NM_API_CLOSE("nm.api.close"),
	NM_OFFSET("nm.offset"),

	EXPERT_L1_RATE_MIN("expert.l1.rate.min"),
	EXPERT_L1_RATE_MAX("expert.l1.rate.max"),

	EXPERT_LOGIC_DEADTIME_THESHOLD_FED("expert.logic.deadtime.threshold.fed"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_PARTITION("expert.logic.deadtime.threshold.partition"),
	EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL("expert.logic.deadtime.threshold.total"),

	EXPERT_OFFSET("expert.offset"),
	SNAPSHOTS_DIR("snapshots"),
	LANDING("landing"),
	EXPERIMENTAL_DIR("experimental");

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
