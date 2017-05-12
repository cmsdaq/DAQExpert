package rcms.utilities.daqexpert.report;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class Report {

	private final Map<String, Long> values;

	public Report() {
		this.values = new HashMap<String, Long>();
	}

	public Map<String, Long> getValues() {
		return values;
	}

	@Override
	public String toString() {
		return "Report [values=" + values + "]";
	}

	public String getReport() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nTimes:\n");
		sb.append("Total time of stable beams: " + getHumanReadable(values.get("totalStableBeamTime")));
		sb.append("\n");
		sb.append("Total uptime: " + getHumanReadable(values.get("totalUptime")));
		sb.append("\n");
		sb.append("Total downtime: " + getHumanReadable(values.get("totalNoRateDuration")));
		sb.append("\n");
		sb.append("Total decision time: " + getHumanReadable(values.get("totalDecisionTime")));
		sb.append("\n");
		sb.append("Total recovery time: " + getHumanReadable(values.get("totalRecoveryTime")));
		sb.append("\nPercentages:\n");
		sb.append("Efficiency: " + (100f * values.get("totalUptime") / values.get("totalStableBeamTime")) + "%.");
		sb.append("\n");
		sb.append("Downtime percentage: "
				+ (100f * values.get("totalNoRateDuration") / values.get("totalStableBeamTime")) + "%.");
		sb.append("\n");
		sb.append("Stable beams lost on decisions: "
				+ (100f * values.get("totalDecisionTime") / values.get("totalStableBeamTime")) + "%.");
		sb.append("\n");
		sb.append("Stable beams lost on recovery: "
				+ (100f * values.get("totalRecoveryTime") / values.get("totalStableBeamTime")) + "%.");

		return sb.toString();
	}

	private String getHumanReadable(Long milliseconds) {
		return DurationFormatUtils.formatDurationWords(milliseconds, true, true);
	}
}
