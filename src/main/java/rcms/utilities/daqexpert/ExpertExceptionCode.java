package rcms.utilities.daqexpert;

public enum ExpertExceptionCode {

	/* General errors */
	ExpertProblem(100, "Expert Problem"),
	CannotParseProcessingEndDate(101, "Canot parse end date of expert processing, should be ISO8601"),

	/* Reasoning errors */
	ReasoningProblem(200, "Reasoning problem"),
	ExperimentalReasoningProblem(201, "Experimental reasoning problem");

	ExpertExceptionCode(int code, String name) {
		this.code = code;
		this.name = name;
	}

	private final int code;
	private final String name;

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
