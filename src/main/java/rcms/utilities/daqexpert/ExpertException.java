package rcms.utilities.daqexpert;




import rcms.utilities.daqaggregator.DAQExceptionCode;

public class ExpertException extends RuntimeException {

	private final ExpertExceptionCode code;

	public ExpertException(ExpertExceptionCode reasoningproblem, String message) {
		super(message);
		this.code = reasoningproblem;
	}

	public ExpertExceptionCode getCode() {
		return code;
	}

}
