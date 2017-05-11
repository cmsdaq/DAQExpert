package rcms.utilities.daqexpert.report;

import java.util.HashMap;
import java.util.Map;

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
		sb.append("Total time of stable beams: " + (values.get("totalStableBeamTime") / 1000) + "sec.");
		sb.append("\n");
		sb.append("Total uptime: " + (values.get("totalUptime") / 1000) + "sec.");
		sb.append("\n");
		sb.append("Total downtime: " + (values.get("totalNoRateDuration") / 1000) + "sec.");
		sb.append("\n");
		sb.append("Total decision time: " + (values.get("totalDecisionTime") / 1000) + "sec.");
		sb.append("\n");
		sb.append("Total recovery time: " + (values.get("totalRecoveryTime") / 1000) + "sec.");
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
}
